package intern.schoolSystem.administration_system.subject;

import intern.schoolSystem.administration_system.database.DatabaseConnection;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubjectModelTest {
    private static Connection connection = null;
    private static SubjectModel subjectModel = null;
    private Statement statement = null;

    @BeforeAll
    public static void beforeAll() throws SQLException, ClassNotFoundException {
        DatabaseConnection dbConnection = new DatabaseConnection();
        connection = dbConnection.connect();
        subjectModel = new SubjectModel(connection);
    }

    @BeforeEach
    public void beforeEach() throws SQLException {
        statement = connection.createStatement();
    }

    @Test
    @Order(1)
    public void testOneSubjectRegistrationCorrectlyPopulatesSubjectsRelations() throws SQLException {
        Subject maths = new Subject();
        maths.setName("Maths");
        maths.setTeacherName("Teacher A");
        subjectModel.registerSubject(maths);

        ResultSet resultSet = retrieveAllRecords();

        int rowCount = 0;
        while(resultSet.next()) {
            assertEquals(1, resultSet.getInt("id"));
            assertEquals("Maths", resultSet.getString("name"));
            assertEquals("Teacher A", resultSet.getString("teacher_name"));
            rowCount++;
        }

        assertEquals(1, rowCount);
    }

    @Test
    @Order(2)
    public void testSubjectRegistrationRegistersTwoSubjectsWithSameDetails() throws SQLException {
        Subject maths = new Subject();
        maths.setName("Maths");
        maths.setTeacherName("Teacher A");
        subjectModel.registerSubject(maths);

        ResultSet resultSet = retrieveAllRecords();
        int rowCount = 0;
        while(resultSet.next()) {
            assertEquals(rowCount+1, resultSet.getInt("id"));
            assertEquals("Maths", resultSet.getString("name"));
            assertEquals("Teacher A", resultSet.getString("teacher_name"));
            rowCount++;
        }

        assertEquals(2, rowCount);
    }

    @Test
    @Order(3)
    public void testSubjectDeletionUsingNonExistingIdThrowsException() throws SQLException {
        Exception exception = assertThrows(InvalidSubjectIDException.class, () -> {
            subjectModel.deleteSubject(400);
        });

        String expectedMsg = "Subject 400 not found. Please check the details provided.";
        String actualMsg = exception.getMessage();
        assertEquals(expectedMsg, actualMsg);
    }

    @Test
    @Order(4)
    public void testSubjectDeletionUsingValidIdsRemovesAppropriateRow() throws SQLException, InvalidSubjectIDException {
        Subject history = new Subject();
        history.setName("History");
        history.setTeacherName("Mohamed Ali");

        subjectModel.registerSubject(history);
        subjectModel.deleteSubject(2);
        subjectModel.deleteSubject(3);

        ResultSet resultSet = retrieveAllRecords();
        int rowCount = 0;
        while(resultSet.next()) {
            assertEquals(1, resultSet.getInt("id"));
            assertEquals("Maths", resultSet.getString("name"));
            assertEquals("Teacher A", resultSet.getString("teacher_name"));
            rowCount++;
        }

        assertEquals(1, rowCount);
    }

    @Test
    @Order(5)
    public void testSubjectIdSequenceUpdatedProperlyFollowingDeletionOfRecords() throws SQLException {
        Subject history = new Subject();
        history.setName("History");
        history.setTeacherName("Mohamed Ali");
        subjectModel.registerSubject(history);

        Subject maths = new Subject();
        maths.setName("Maths");
        maths.setTeacherName("Teacher A");

        ArrayList<Subject> subjects = new ArrayList<>();
        subjects.add(maths);
        subjects.add(history);

        ResultSet resultSet = retrieveAllRecords();
        int rowCount = 0;
        while(resultSet.next()) {
            assertEquals(rowCount+1, resultSet.getInt("id"));
            assertEquals(subjects.get(rowCount).getName(), resultSet.getString("name"));
            assertEquals(subjects.get(rowCount).getTeacherName(), resultSet.getString("teacher_name"));
            rowCount++;
        }

        assertEquals(2, rowCount);
    }

    @Test
    @Order(6)
    public void testSubjectUpdateThrowsAnExceptionGivenAnInvalidSubjectID() {
        Exception exception = assertThrows(InvalidSubjectIDException.class, () -> {
           subjectModel.updateSubjectDetails(400, "German", "Manuel Neuer");
        });

        String expectedMsg = "Subject 400 not found. Please check the details provided.";
        String actualMsg = exception.getMessage();
        assertEquals(expectedMsg, actualMsg);
    }

    @Test
    @Order(7)
    public void testSubjectUpdateProperlyUpdatesDataGivenValidDetails() throws SQLException, InvalidSubjectIDException {
        subjectModel.updateSubjectDetails(2, "German", "Manuel Neuer");

        Subject german = new Subject();
        german.setName("German");
        german.setTeacherName("Manuel Neuer");

        Subject maths = new Subject();
        maths.setName("Maths");
        maths.setTeacherName("Teacher A");

        ArrayList<Subject> subjects = new ArrayList<>();
        subjects.add(maths);
        subjects.add(german);

        ResultSet resultSet = retrieveAllRecords();
        int rowCount = 0;
        while(resultSet.next()) {
            assertEquals(rowCount+1, resultSet.getInt("id"));
            assertEquals(subjects.get(rowCount).getName(), resultSet.getString("name"));
            assertEquals(subjects.get(rowCount).getTeacherName(), resultSet.getString("teacher_name"));
            rowCount++;
        }

        assertEquals(2, rowCount);

    }

    @Test
    @Order(8)
    public void testDisplaySubjectDetailsGivenANonExistentSubjectName() throws SQLException {
        ResultSet resultSet = subjectModel.retrieveSubjectDetails("French");
        int rowCount = 0;
        while(resultSet.next()) {
            rowCount++;
        }
        assertEquals(0, rowCount);
    }

    @Test
    @Order(9)
    public void testDisplaySubjectDetailsGivenSubjectNameThatIsSharedAcrossMultipleRecords() throws SQLException {
        Subject german = new Subject();
        german.setName("German");
        german.setTeacherName("Thomas Muller");

        subjectModel.registerSubject(german);
        ResultSet resultSet = subjectModel.retrieveSubjectDetails("German");
        String[] teacherNames = new String[]{"Manuel Neuer", "Thomas Muller"};
        int rowCount = 0;
        while(resultSet.next()) {
            assertEquals(rowCount+2, resultSet.getInt("id"));
            assertEquals("German", resultSet.getString("name"));
            assertEquals(teacherNames[rowCount], resultSet.getString("teacher_name"));
            assertEquals(0, resultSet.getInt("enrolled_students"));
            rowCount++;
        }

        assertEquals(2, rowCount);
    }

    private ResultSet retrieveAllRecords() throws SQLException {
        String query = "SELECT * FROM subjects";
        statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    @AfterEach
    public void afterEach() throws SQLException {
        statement.close();
    }

    @AfterAll
    public static void afterAll() throws SQLException {
        connection.close();
    }
}
