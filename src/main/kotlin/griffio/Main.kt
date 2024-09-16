package griffio

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.jdbc.JdbcDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import griffio.queries.Sample
import org.postgresql.copy.CopyManager
import org.postgresql.core.BaseConnection
import org.postgresql.ds.PGSimpleDataSource
import java.io.Reader
import kotlin.io.path.Path
import kotlin.io.path.reader

private fun getSqlDriver() = PGSimpleDataSource().apply {
    setURL("jdbc:postgresql://localhost:5432/laterals")
    applicationName = "App Main"
}.asJdbcDriver()

// SqlDelight doesn't expose the generated query directly - only the driver has access to it for execution
// Create a driver implementation for PostgreSql CopyManager to use the compiled query for execution
class CopyInManagerDriver(private val driver: JdbcDriver, private val file: Reader) : SqlDriver by driver  {
    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> {
        // CopyManager utility class is the JDBC way to use the COPY command with STDIN
        // https://github.com/pgjdbc/pgjdbc/blob/master/pgjdbc/src/main/java/org/postgresql/copy/CopyManager.java
        val copyManager = CopyManager(getSqlDriver().getConnection() as BaseConnection)
        val copyIn = copyManager.copyIn(sql, file)
        return QueryResult.Value(copyIn)
    }
}

fun main() {
    val driver = getSqlDriver()
    val sample = Sample(driver)

    sample.salesQueries.insert()
    println(sample.salesQueries.select().executeAsList().joinToString("\n"))

    val copySample = Sample(CopyInManagerDriver(driver, Path("csv/Kickstarter.csv").reader()))
    copySample.kickStarterQueries.copy()
    println(sample.kickStarterQueries.select().executeAsList().joinToString("\n"))
}
