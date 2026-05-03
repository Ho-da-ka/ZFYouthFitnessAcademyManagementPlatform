import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RunSql {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: RunSql <application.properties> <sql-file>");
        }
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        String url = properties.getProperty("spring.datasource.url");
        String username = properties.getProperty("spring.datasource.username");
        String password = properties.getProperty("spring.datasource.password");
        String sql = Files.readString(Path.of(args[1]), StandardCharsets.UTF_8);

        int executed = 0;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {
            for (String command : splitStatements(sql)) {
                String trimmed = command.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                boolean hasResultSet = statement.execute(trimmed);
                if (hasResultSet) {
                    printResultSet(statement.getResultSet());
                }
                executed++;
            }
        }
        System.out.println("Executed SQL statements: " + executed);
    }

    private static void printResultSet(ResultSet resultSet) throws Exception {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                System.out.print('\t');
            }
            System.out.print(metaData.getColumnLabel(i));
        }
        System.out.println();
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    System.out.print('\t');
                }
                System.out.print(resultSet.getString(i));
            }
            System.out.println();
        }
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }
            if (ch == ';' && !inQuote) {
                statements.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (!current.isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }
}
