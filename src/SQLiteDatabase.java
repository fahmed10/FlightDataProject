import java.sql.*;
import java.util.function.Consumer;

public class SQLiteDatabase implements AutoCloseable {
	private static final int TIMEOUT = 3;
	private Connection connection = null;
	private final Consumer<SQLException> errorHandler;

	/**
	 * Constructs an <code>SQLiteDatabase</code>.
	 * @param dbFilePath The SQLite file to connect to.
	 */
	public SQLiteDatabase(String dbFilePath, Consumer<SQLException> errorHandler) {
		this.errorHandler = errorHandler;
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
		} catch (SQLException e) {
			errorHandler.accept(e);
		}
	}

	/**
	 * Executes the given SQL query and returns the generated <code>ResultSet</code>.
	 * The caller of this method is expected to close the returned <code>ResultSet</code>'s
	 * underlying <code>Statement</code> by calling <code>resultSet.getStatement().close()</code>.
	 * @param sql The SQL string to execute.
	 * @return The <code>ResultSet</code> which contains the data returned by the query.
	 */
	public ResultSet query(String sql) {
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(TIMEOUT);
			return statement.executeQuery(sql);
		} catch (SQLException e) {
			errorHandler.accept(e);
			return null;
		}
	}

	/**
	 * Executes the given SQL query with the given parameters and returns the generated
	 * <code>ResultSet</code>. The caller of this method is expected to close the returned
	 * <code>ResultSet</code>'s underlying <code>Statement</code> by calling
	 * <code>resultSet.getStatement().close()</code>.
	 * @param sql The SQL string to execute.
	 * @param parameters The parameters to use.
	 * @return The <code>ResultSet</code> which contains the data returned by the query.
	 */
	public ResultSet queryPrepared(String sql, Object... parameters) {
		try {
			return createPreparedStatement(sql, parameters).executeQuery();
		} catch (SQLException e) {
			errorHandler.accept(e);
			return null;
		}
	}

	/**
	 * Executes the given SQL statement and returns the number of rows affected.
	 * @param sql The SQL string to execute.
	 * @return The number of rows affected.
	 */
	public int update(String sql) {
		try {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(TIMEOUT);
			int rowCount = statement.executeUpdate(sql);
			statement.close();
			return rowCount;
		} catch (SQLException e) {
			errorHandler.accept(e);
			return 0;
		}
	}

	/**
	 * Executes the given SQL statement and returns the number of rows affected.
	 * @param sql The SQL string to execute.
	 * @param parameters The parameters to use.
	 * @return The number of rows affected.
	 */
	public int updatePrepared(String sql, Object... parameters) {
		try {
			PreparedStatement statement = createPreparedStatement(sql, parameters);
			int rowCount = statement.executeUpdate();
			statement.close();
			return rowCount;
		} catch (SQLException e) {
			errorHandler.accept(e);
			return 0;
		}
	}

	/**
	 * Closes this <code>DB</code> by closing its underlying connection object.
	 */
	@Override
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			errorHandler.accept(e);
		}
	}

	/**
	 * Creates a <code>PreparedStatement</code> from an SQL string and a list of parameters.
	 * @param sql The SQL string to use.
	 * @param parameters The parameters to use.
	 * @return The <code>PreparedStatement</code> with the given SQL string and parameters.
	 */
	private PreparedStatement createPreparedStatement(String sql, Object... parameters) {
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setQueryTimeout(TIMEOUT);

			for (int i = 0; i < parameters.length; i++) {
				statement.setObject(i + 1, parameters[i]);
			}

			return statement;
		} catch (SQLException e) {
			errorHandler.accept(e);
			return null;
		}
	}
}