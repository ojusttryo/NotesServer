package ru.justtry.shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ru.justtry.notes.Note;
import ru.justtry.rest.controllers.NotesController;
import ru.justtry.rest.errors.RestError;

@Component
public class Utils
{
    final static Logger logger = LogManager.getLogger(Utils.class);

    @Autowired
    @Lazy
    private NotesController notesController;

    public boolean equals(Double x, Double y, Double threshold)
    {
        return ((x - y) < threshold);
    }


    public ResponseEntity<Object> getResponseForError(HttpHeaders headers, Exception e)
    {
        return new ResponseEntity<>(new RestError(e.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public long getTimeInMs()
    {
        return Instant.now().getEpochSecond() * 1000;
    }


    public void migrateAffairs()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Abandoned");
        stateMapper.put(3, "Finished");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Postponed");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Affairs");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String description = rs.getString("Description");
                Boolean isDateSet = rs.getBoolean("IsDateSet");
                String date = rs.getString("Date");

                if (isDateSet != null && isDateSet)
                {
                    DateFormat oldFormat = new SimpleDateFormat("dd MM yyyy");
                    Date d = oldFormat.parse(date);
                    SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
                    date = newFormat.format(d);
                }

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("title", name);
                attributes.put("affairs-state", stateMapper.get(currentState));
                attributes.put("importance", "Normal");
                if (isDateSet != null && isDateSet)
                    attributes.put("date", date);
                attributes.put("description", description);
                attributes.put("comment", comment);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("affairs", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateAnimeSerials()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Watching");
        stateMapper.put(2, "Not interesting");
        stateMapper.put(3, "Watched");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Waiting");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from AnimeSerials");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                Integer season = rs.getInt("Season");
                Integer episode = rs.getInt("Episode");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("tvseries-state", stateMapper.get(currentState));
                attributes.put("episode", episode);
                attributes.put("language", "RU");
                attributes.put("season", season);
                attributes.put("comment", comment);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("anime-serials", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateBookmarks()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Watching");
        stateMapper.put(2, "Not interesting");
        stateMapper.put(3, "Watched");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Waiting");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Bookmarks");
            while(rs.next())
            {
                String name = rs.getString("Name");
                String comment = rs.getString("Comment");
                String url = rs.getString("URL");
                String login = rs.getString("Login");
                String password = rs.getString("Password");
                String email = rs.getString("Email");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("title", name);
                attributes.put("bookmark-category", new ArrayList<String>());
                attributes.put("url", url);
                attributes.put("login", login);
                attributes.put("password", password);
                attributes.put("email", email);
                attributes.put("comment", comment);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("bookmarks", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateDesires()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Abandoned");
        stateMapper.put(3, "Completed");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Postponed");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Desires");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String description = rs.getString("Description");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("title", name);
                attributes.put("desire-state", stateMapper.get(currentState));
                attributes.put("importance", "Normal");
                attributes.put("description", description);
                attributes.put("comment", comment);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("desires", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateMovies()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Watching");
        stateMapper.put(2, "Not interesting");
        stateMapper.put(3, "Watched");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Waiting");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Films");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                Integer year = rs.getInt("Year");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("movie-state", stateMapper.get(currentState));
                if (year != null && year > 0)
                    attributes.put("year", year);
                attributes.put("language", "RU");
                attributes.put("comment", comment);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("movies", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateGames()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Deleted");
        stateMapper.put(3, "Finished");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Waiting");

        Map<Integer, String> typeMapper = new HashMap<>();
        typeMapper.put(0, "Not defined");
        typeMapper.put(1, "Single player");
        typeMapper.put(2, "Multiplayer");
        typeMapper.put(3, "Mixed");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Games");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String url = rs.getString("DownloadLink");
                String version = rs.getString("Version");
                String login = rs.getString("Login");
                String password = rs.getString("Password");
                String email = rs.getString("Email");
                String genre = rs.getString("Genre");
                Integer type = rs.getInt("PlayersCount");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("game-state", stateMapper.get(currentState));
                attributes.put("comment", genre + "\n" + comment);
                attributes.put("login", login);
                attributes.put("password", password);
                attributes.put("email", email);
                attributes.put("url", url);
                attributes.put("version", version);
                attributes.put("game-type", typeMapper.get(type));

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("games", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateLiterature()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Reading");
        stateMapper.put(2, "Abandoned");
        stateMapper.put(3, "Finished");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Waiting");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Literature");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                Integer year = rs.getInt("Year");
                String author = rs.getString("Author");
                String genre = rs.getString("Genre");
                String universe = rs.getString("Universe");
                String series = rs.getString("Series");
                Integer volume = rs.getInt("Volume");
                Integer chapter = rs.getInt("Chapter");
                Integer page = rs.getInt("Page");
                Integer pages = rs.getInt("Pages");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("literature-state", stateMapper.get(currentState));
                attributes.put("comment", genre + "\n\n" + comment);
                if (year != null && year > 0)
                    attributes.put("year", year);
                attributes.put("author", author);
                attributes.put("universe", universe);
                attributes.put("series", series);
                if (volume != null && volume > 0)
                    attributes.put("volume", volume);
                if (chapter != null && chapter > 0)
                    attributes.put("chapter", chapter);
                if (page != null && page > 0)
                    attributes.put("page", page);
                if (pages != null && pages > 0)
                    attributes.put("total-pages", pages);
                attributes.put("language", "RU");
                attributes.put("book-type", "Text");

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("literature", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateMeal()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Abandoned");
        stateMapper.put(3, "Postponed");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Planned");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Meal");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String ingredients = rs.getString("Ingredients");
                String recipe = rs.getString("Recipe");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("meal-state", stateMapper.get(currentState));
                attributes.put("comment", comment);
                attributes.put("ingredients", ingredients);
                attributes.put("recipe", recipe);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("meal", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migratePeople()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Black list");
        stateMapper.put(3, "Finished");
        stateMapper.put(4, "Pause");
        stateMapper.put(5, "Pause");

        Map<Integer, String> sexMapper = new HashMap<>();
        sexMapper.put(0, "None");
        sexMapper.put(1, "Male");
        sexMapper.put(2, "Female");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from People");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String address = rs.getString("Address");
                String birthdate = rs.getString("Birthdate");
                String nickname = rs.getString("Nickname");
                String contacts = rs.getString("Contacts");
                Integer sex = rs.getInt("Sex");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("people-state", stateMapper.get(currentState));
                attributes.put("comment", String.format("Birthday: \n%s\n\nContacts: \n%s\n\nComment: \n%s",
                        birthdate, contacts, comment));
                attributes.put("address", address);
                attributes.put("nickname", nickname);
                attributes.put("sex", sexMapper.get(sex));

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("people", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migratePrograms()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Deleted");
        stateMapper.put(3, "Finished");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Not defined");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Programs");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String url = rs.getString("DownloadLink");
                String version = rs.getString("Version");
                String login = rs.getString("Login");
                String password = rs.getString("Password");
                String email = rs.getString("Email");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("program-state", stateMapper.get(currentState));
                attributes.put("comment", comment);
                attributes.put("login", login);
                attributes.put("password", password);
                attributes.put("email", email);
                attributes.put("url", url);
                attributes.put("version", version);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("programs", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateRegularAffairs()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Active");
        stateMapper.put(2, "Abandoned");
        stateMapper.put(3, "Abandoned");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Planned");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from RegularDoings");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                String description = rs.getString("Description");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("title", name);
                attributes.put("regular-affairs-state", stateMapper.get(currentState));
                attributes.put("comment", comment);
                attributes.put("long-description", description);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("regular-affairs", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void migrateSerials()
    {
        /**
         * CurrentState =
         * 1 = Active
         * 2 = Deleted
         * 3 = Finished
         * 4 = Postponed
         * 5 = Waiting
         */

        Map<Integer, String> stateMapper = new HashMap<>();
        stateMapper.put(1, "Watching");
        stateMapper.put(2, "Not interesting");
        stateMapper.put(3, "Watched");
        stateMapper.put(4, "Postponed");
        stateMapper.put(5, "Waiting");

        try
        {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:notes.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("select * from Serials");
            while(rs.next())
            {
                String name = rs.getString("Name");
                Integer currentState = rs.getInt("CurrentState");
                String comment = rs.getString("Comment");
                Integer season = rs.getInt("Season");
                Integer episode = rs.getInt("Episode");

                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", name);
                attributes.put("tvseries-state", stateMapper.get(currentState));
                attributes.put("episode", episode);
                attributes.put("language", "RU");
                attributes.put("season", season);
                attributes.put("comment", comment);

                Note note = new Note();
                note.setAttributes(attributes);

                notesController.save("serials", note);
            }
            rs.close();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
}
