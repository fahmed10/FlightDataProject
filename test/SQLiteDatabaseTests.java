import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteDatabaseTests {
    protected static SQLiteDatabase db;

    @BeforeClass
    public static void setUp() throws IOException {
        Files.deleteIfExists(Path.of("test.db"));
        db = new SQLiteDatabase("test.db", (e) -> Assert.fail(e.getMessage()));
        db.update("CREATE TABLE test_table (test_int INT PRIMARY KEY, test_string VARCHAR(255), test_bool INT)");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        db.close();
        Files.deleteIfExists(Path.of("test.db"));
    }

    @Test
    public void update() {
        int rowCount = db.update("INSERT INTO test_table (test_int, test_string, test_bool) VALUES (3, 'Test String', 1)");
        Assert.assertEquals(rowCount, 1);
        rowCount = db.update("INSERT INTO test_table (test_int, test_string, test_bool) VALUES (44, 'Test Data', 0), (11, 'Test 2', 1)");
        Assert.assertEquals(rowCount, 2);
        rowCount = db.update("UPDATE test_table SET test_bool = 1 WHERE test_bool = 1");
        Assert.assertEquals(rowCount, 2);
    }

    @Test
    public void updatePrepared() {
        int rowCount = db.updatePrepared("INSERT INTO test_table (test_int, test_string, test_bool) VALUES (?, ?, ?)", 4, "Test String", true);
        Assert.assertEquals(rowCount, 1);
        rowCount = db.updatePrepared("INSERT INTO test_table (test_int, test_string, test_bool) VALUES (?, ?, ?), (?, ?, ?)", 45, "Test Data", false, 12, "Test 2", true);
        Assert.assertEquals(rowCount, 2);
        rowCount = db.updatePrepared("UPDATE test_table SET test_bool = ? WHERE test_bool = ?", true, true);
        Assert.assertEquals(rowCount, 4);
    }

    @Test
    public void query() throws SQLException {
        ResultSet resultSet = db.query("SELECT * FROM test_table");
        testResultSet(resultSet);
    }

    @Test
    public void queryPrepared() throws SQLException {
        ResultSet resultSet = db.queryPrepared("SELECT * FROM test_table WHERE 'test' = ? AND 3 = ? AND true = ?", "test", 3, true);
        testResultSet(resultSet);
    }

    private void testResultSet(ResultSet resultSet) throws SQLException {
        int[] expectedInts = {3, 44, 11, 4, 45, 12};
        String[] expectedStrings = {"Test String", "Test Data", "Test 2", "Test String", "Test Data", "Test 2"};
        boolean[] expectedBools = {true, false, true, true, false, true};

        int i = 0;
        while (resultSet.next()) {
            Assert.assertEquals(expectedInts[i], resultSet.getInt("test_int"));
            Assert.assertEquals(expectedStrings[i], resultSet.getString("test_string"));
            Assert.assertEquals(expectedBools[i], resultSet.getBoolean("test_bool"));
            i++;
        }

        resultSet.getStatement().close();
    }

    @Test
    public void close() {
        db.close();
        db = new SQLiteDatabase("test.db", (e) -> Assert.fail(e.getMessage()));
    }
}
