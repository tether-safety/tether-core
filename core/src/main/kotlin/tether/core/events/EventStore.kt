package tether.core.events

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Append-only event store.
 * This is the single source of truth for all safety behavior.
 */
class EventStore(
    private val databasePath: String
) {

    private fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:$databasePath")
    }

    init {
        createTableIfNotExists()
    }

    private fun createTableIfNotExists() {
        getConnection().use { conn ->
            val stmt = conn.createStatement()
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS events (
                    event_id TEXT PRIMARY KEY,
                    event_type TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    payload_json TEXT NOT NULL,
                    source TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    /**
     * Append a new event.
     * This must never update or delete existing rows.
     */
    fun append(event: Event) {
        getConnection().use { conn ->
            val sql =
                """
                INSERT INTO events (
                    event_id,
                    event_type,
                    created_at,
                    payload_json,
                    source
                ) VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

            val stmt = conn.prepareStatement(sql)
            stmt.setString(1, event.eventId)
            stmt.setString(2, event.eventType.name)
            stmt.setLong(3, event.createdAtMillis)
            stmt.setString(4, event.payloadJson)
            stmt.setString(5, event.source.name)
            stmt.executeUpdate()
        }
    }

    /**
     * Read all events in creation order.
     */
    fun readAll(): List<Event> {
        val events = mutableListOf<Event>()

        getConnection().use { conn ->
            val stmt = conn.createStatement()
            val rs: ResultSet =
                stmt.executeQuery(
                    """
                    SELECT event_id, event_type, created_at, payload_json, source
                    FROM events
                    ORDER BY created_at ASC
                    """.trimIndent()
                )

            while (rs.next()) {
                events.add(
                    Event(
                        eventId = rs.getString("event_id"),
                        eventType = EventType.valueOf(rs.getString("event_type")),
                        createdAtMillis = rs.getLong("created_at"),
                        payloadJson = rs.getString("payload_json"),
                        source = EventSource.valueOf(rs.getString("source"))
                    )
                )
            }
        }

        return events
    }
}
