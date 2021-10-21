import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class CSVConsume {
    static final String pathToCSV = System.getProperty("user.dir") + "/data.csv";

    public static void main(String[] args) {
        writeDataFromCSVToDatabase();
    }

    /**
     * The main method in which most of the functionality of the code will be executed
     */
    public static void writeDataFromCSVToDatabase() {
        int batchSize = 20;

        try {
            Connection connection = connectToDatabase();
            String sql1 = "insert into employee(ico, nazevfirmy, adresfirmy, email, jmeno, prijmeni, datum) values (?,?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql1);
            BufferedReader lineReader = new BufferedReader(new FileReader(pathToCSV));

            String lineText = null;
            int count = 0;
            lineReader.readLine();

            while ((lineText = lineReader.readLine()) != null) {
                String[] data = lineText.split(",");

                for (int i = 0; i < data.length; i++) {
                    statement.setString(i+1,data[i]);
                }

                if (count % batchSize == 0) {
                    statement.executeBatch();
                }
            }

            lineReader.close();
            statement.executeBatch();
            connection.commit();
            connection.close();

            fetchDataFromDatabase();
            moveFileToAnotherPath();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    /**
     * The method will transfer the file to another directory
     */
    public static void moveFileToAnotherPath() throws IOException {
        Path temp = Files.move(Paths.get(pathToCSV),
                Paths.get(pathToCSV));

        System.out.println("File moved successfully");
    }

    /**
     * The method will connect to the database
     *
     * @throws SQLException if something goes wrong
     */
    public static Connection connectToDatabase() throws SQLException {
        String jdbcURL = "jdbc:mysql://localhost:3306/ems";
        String userName = "root";
        String password = "yourpasswd";

        Connection connection = null;

        connection = DriverManager.getConnection(jdbcURL, userName, password);
        connection.setAutoCommit(false);

        return connection;
    }

    /**
     * The method will be fetch data from database for showing changes
     *
     * @throws SQLException if something goes wrong
     */
    public static void fetchDataFromDatabase() throws SQLException {
        Connection connection = connectToDatabase();
        Statement mystatement = connection.createStatement();

        ResultSet codebase = mystatement.executeQuery("select * from employee");

        while (codebase.next()) {
            System.out.println(codebase.getString("ico")
                    + " " + codebase.getString("nazevfirmy")
                    + " " + codebase.getString("adresfirmy")
                    + " " + codebase.getString("email")
                    + " " + codebase.getString("jmeno")
                    + " " + codebase.getString("prijmeni")
                    + " " + codebase.getString("datum"));
        }

        connection.commit();
        connection.close();
    }
}
