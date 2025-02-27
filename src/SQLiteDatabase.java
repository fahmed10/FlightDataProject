import java.sql.*;

public class SQLiteDatabase implements AutoCloseable {
	private final Connection connection;
	private static final int TIMEOUT = 3;

	/**
	 * Constructs an <code>SQLiteDatabase</code>.
	 * @param dbFilePath The SQLite file to connect to.
	 * @param autoCommit Whether to enable auto-commit mode or not.
	 * @throws SQLException If an error occurs while connecting to the database.
	 */
	public SQLiteDatabase(String dbFilePath, boolean autoCommit) throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * Makes all changes to the database since the last transaction permanent.
	 * This method should only be called if auto-commit mode is disabled.
	 * @throws SQLException If a database error occurs.
	 */
	public void commitTransaction() throws SQLException {
		connection.commit();
	}

	/**
	 * Undoes all changes to the database since the last transaction.
	 * This method should only be called if auto-commit mode is disabled.
	 * @throws SQLException If a database error occurs.
	 */
	public void rollbackTransaction() throws SQLException {
		connection.rollback();
	}

	/**
	 * Executes the given SQL query and returns the generated <code>ResultSet</code>.
	 * The caller of this method is expected to close the returned <code>ResultSet</code>'s
	 * underlying <code>Statement</code> by calling <code>resultSet.getStatement().close()</code>.
	 * @param sql The SQL string to execute.
	 * @return The <code>ResultSet</code> which contains the data returned by the query.
	 * @throws SQLException If a database error occurs.
	 */
	public ResultSet query(String sql) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(TIMEOUT);
		return statement.executeQuery(sql);
	}

	/**
	 * Executes the given SQL query with the given parameters and returns the generated
	 * <code>ResultSet</code>. The caller of this method is expected to close the returned
	 * <code>ResultSet</code>'s underlying <code>Statement</code> by calling
	 * <code>resultSet.getStatement().close()</code>.
	 * @param sql The SQL string to execute.
	 * @param parameters The parameters to use.
	 * @return The <code>ResultSet</code> which contains the data returned by the query.
	 * @throws SQLException If a database error occurs.
	 */
	public ResultSet queryPrepared(String sql, Object... parameters) throws SQLException {
		return createPreparedStatement(sql, parameters).executeQuery();
	}

	/**
	 * Executes the given SQL statement and returns the number of rows affected.
	 * @param sql The SQL string to execute.
	 * @return The number of rows affected.
	 * @throws SQLException If a database error occurs.
	 */
	public int update(String sql) throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(TIMEOUT);
		int rowCount = statement.executeUpdate(sql);
		statement.close();
		return rowCount;
	}

	/**
	 * Executes the given SQL statement and returns the number of rows affected.
	 * @param sql The SQL string to execute.
	 * @param parameters The parameters to use.
	 * @return The number of rows affected.
	 * @throws SQLException If a database error occurs.
	 */
	public int updatePrepared(String sql, Object... parameters) throws SQLException {
		PreparedStatement statement = createPreparedStatement(sql, parameters);
		int rowCount = statement.executeUpdate();
		statement.close();
		return rowCount;
	}

	/**
	 * Closes this <code>DB</code> by closing its underlying connection object. If auto-commit
	 * mode is off, the current transaction is rolled back before the connection to the database
	 * is closed.
	 * @throws SQLException If a database error occurs.
	 */
	@Override
	public void close() throws SQLException {
		if (!connection.getAutoCommit()) {
			connection.rollback();
		}

		connection.close();
	}

	/**
	 * Creates a <code>PreparedStatement</code> from an SQL string and a list of parameters.
	 * @param sql The SQL string to use.
	 * @param parameters The parameters to use.
	 * @return The <code>PreparedStatement</code> with the given SQL string and parameters.
	 * @throws SQLException If a database error occurs.
	 */
	private PreparedStatement createPreparedStatement(String sql, Object... parameters) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);
		statement.setQueryTimeout(TIMEOUT);

		for (int i = 0; i < parameters.length; i++) {
			statement.setObject(i + 1, parameters[i]);
		}

		return statement;
	}
}