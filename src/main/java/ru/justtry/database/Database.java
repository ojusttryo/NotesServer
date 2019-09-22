package ru.justtry.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.log4j.Logger;

//@Component
//@PropertySource("classpath:application.properties")
//@Component
//@Scope("singleton")
//@Named("database")
public class Database
{
    final static Logger logger = Logger.getLogger(Database.class);

    private MongoClient mongo;
    private MongoDatabase database;

//    @Value("${server.port:8765}")
    private Integer port;

//    @Value("${server.host:localhost}")
    private String host;

//    @Value("${database.name}")
    private String name;

//    @Value("${database.user}")
    private String user;

//    @Value("${database.password}")
    private String password;

    public Database()
    {
        logger.info("Database created");
    }

    public void init(String host, Integer port, String name, String user, String password)
    {
        this.host = host;
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
//        mongo = new MongoClient(host, port);
//        database = mongo.getDatabase(name);
//        createUser();
    }

    public void createUser()
    {
        try
        {
            MongoCredential credential;
            credential = MongoCredential.createCredential(user, name, password.toCharArray());
        }
        catch (MongoCommandException e)
        {
            System.out.println("Cannot create user\n" + e.toString());
        }
    }
}


/**
 * Expected hierarchy of data stored in MongoDB:
 *
 *         Versions: [
 *             notes:
 *             serials:
 *             movies
 *             animeSerials
 *             animeMovies
 *             ...
 *         ]
 *
 *         Serials:
 *             lastUpdate:
 *             data: [
 *             {
 *                 id
 *                 saved
 *                 updated
 *                 name: [
 *                     EN:
 *                     RU:
 *                 ]
 *                 description: [
 *                     EN:
 *                     RU:
 *                 ]
 *                 plot: [
 *                     EN:
 *                     RU:
 *                 ]
 *                 seasonsCount
 *                 //seriesCount - absent, because there are serials with dynamic series count
 *                 releaseDate
 *                 rating: [
 *                     imdb
 *                     kinopoisk
 *                     lostfilm
 *                     ...
 *                 ]
 *                 type: [
 *                     Space
 *                     Time travel
 *                     ...
 *                 }
 *                 genre: [
 *                     drama,
 *                     fantastics
 *                 }
 *                 photos: [
 *                     // refs or photos from server
 *                 ]
 *
 *                 author: Netflix
 *                 country: USA
 *             }
 *             ...
 *         }
 *
 *         Movies: [
 *             ...
 *         ]
 *
 *         AnimeSerials: [
 *             ...
 *         ]
 *
 *         AnimeMovies: [
 *             ...
 *         ]
 *
 *         Literature: [
 *             ...
 *         ]
 *
 *         Users: [
 *             {
 *                 id: 1
 *                 name: John Wick
 *                 passowrd: somehash
 *                 email: email@somemail.com
 *                 phone: +7 978 ...
 *                 birthdate: 16.07.1990
 *                 address:
 *                 {
 *                     country: USA,
 *                     city: New York
 *                     ...
 *                 }
 *
 *                 registered: timestamp
 *                 lastVisited: timestamp
 *                 lastActivity: timestamp
 *
 *                 // Add photo list to almost all notes and reference to this table.
 *                 photos: [
 *                     {
 *                         id
 *                         name
 *                         date
 *                         author
 *                         groups: [ Travel, Naumen ]
 *                         people: []
 *                         location
 *                         state
 *                         comment
 *                     }
 *                 ]
 *
 *
 *                 GeneralAffairs: [
 *                     {
 *                         id: 1
 *                         name: Write book,
 *                         description: Write book about psychology,
 *                         executeDate: date,
 *                         state: State.
 *                         comment: Some comment
 *                         added: date
 *                         lastUpdate: date
 *
 *                         priorityLevel: 1-10
 *                         price: total price for all inner affairs
 *
 *                         // Inner affairs. I.E. Make good health: teeth: clean, remove
 *                         Affairs: [
 *                             ...
 *                             Affairs: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 AnimeFilms: [
 *                     {
 *                         id: 1
 *                         name: Name,
 *                         year: Year,
 *                         state: State,
 *                         comment: Some comment
 *                         ownRatio:
 *                     }
 *                     ...
 *                 ]
 *
 *                 AnimeSerials: [
 *                     {
 *                         id: 1
 *                         name
 *                         season
 *                         episode
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Bookmarks: [
 *                     {
 *                         id
 *                         name
 *                         url
 *                         login
 *                         password
 *                         email
 *                         state
 *                         comment
 *                         file: (archieve of site)
 *
 *                         // inner bookmarks
 *                         bookmarks: [
 *                             ...
 *                             bookmarks: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Desires: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         state
 *                         comment
 *                         priorityLevel: 1-10
 *                         price: total price for all inner desires
 *
 *                         // inner desires. For example, bicycle, and the bag/ring/wheel for it.
 *                         desires: [
 *                             ...
 *                             desires: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Movies: [
 *                     {
 *                         id
 *                         name
 *                         year
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Games: [
 *                     {
 *                         id
 *                         name
 *                         version
 *                         genre
 *                         type (singleplayer/multiplayer/mixed)
 *                         link
 *                         login
 *                         password
 *                         email
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 }
 *
 *                 Literature: [
 *                     {
 *                         id
 *                         name
 *                         author
 *                         genre
 *                         universe (S.T.A.L.K.E.R, Metro 2033, etc)
 *                         series (some series in universe or without it)
 *                         audio: (true if audio, else - text)
 *                         volume
 *                         currentChapter
 *                         currentPage
 *                         pagesCount
 *                         year
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Meal: [
 *                     {
 *                         id
 *                         name
 *                         ingredients
 *                         recipe
 *                         state
 *                         comment
 *                     }
 *                     ...
 *                 ]
 *
 *                 Performances: [
 *                     {
 *                         id
 *                         name
 *                         year
 *                         state
 *                         comment
 *                         ownRaio
 *                     }
 *                     ...
 *                 ]
 *
 *                 People: [
 *                     {
 *                         id
 *                         name
 *                         nickname
 *                         sex
 *                         birthdate
 *                         address
 *                         contacts:
 *                         {
 *                             phone (always)
 *                             telegram (custom)
 *                             viber (custom)
 *                             ...
 *                         }
 *                         groups:
 *                         {
 *                             id: 1
 *                             id: 2
 *                             ...
 *                         }
 *                         state
 *                         comment
 *                     }
 *                     ...
 *                 ]
 *
 *                 PropleGroups: [
 *                     { id: 1, name: Tavrida },
 *                     { id: 2, name: Naumen },
 *                     ...
 *                 ]
 *
 *                 Programs: [
 *                     {
 *                         id
 *                         name
 *                         version
 *                         link (from Bookmarks or new)
 *                         login
 *                         password
 *                         email
 *                         state
 *                         comment
 *                     }
 *                     ...
 *                 ]
 *
 *                 DailyAffairs: [
 *                     {
 *                         id
 *                         date: (for every day)
 *                         name
 *                         description
 *                         state
 *                         comment
 *                         priorityLevel: 1-10
 *                         price: (sum from all inner)
 *                         repeat: (every day, one time, etc)
 *
 *                         // Inner affairs. Example: morning routine: teeth, toilet, food, etc.
 *                         DailyAffairs: [
 *                             ...
 *                             DailyAffairs: [ ... ]
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Serials: [
 *                     {
 *                         id
 *                         name
 *                         season
 *                         episode
 *                         state
 *                         comment
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 TVShows: [
 *                     {
 *                         id
 *                         name
 *                         season
 *                         episode
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         ownRatio
 *                     }
 *                     ...
 *                 ]
 *
 *                 Concerts: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         visited: boolean
 *                         location
 *                         date
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         ownRatio
 *                     }
 *                 ]
 *
 *                 // Some walks by city, etc
 *                 Walks: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         location
 *                         date
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         photos: [
 *                             id: 1
 *                             id: 2
 *                         ]
 *                     }
 *                 ]
 *
 *                 Travels: [
 *                     {
 *                         id
 *                         name
 *                         description
 *                         locations: [
 *                             { id: 1, location: name/point, date, photos }
 *                             ...
 *                         ]
 *                         date
 *                         state
 *                         comment
 *                         added
 *                         lastUpdated
 *                         sharedPhotos: [
 *                             id: 1
 *                             id: 2
 *                         ]
 *                     }
 *                     ...
 *                 ]
 *
 *                 Minds: [
 *                     {
 *                         // Просто мысли человека. Что-то полезное, интересное, что-то что хочется запомнить и т.п.
 *                         // Сюда же можно вносить новые идеи или лайфхаки, полезные для человека.
 *                     }
 *                 ]
 *
 *                 // goals in life
 *                 Goals: [
 *                     priorityLeveL: 1-10
 *                     date
 *                     type: single (in that date), every day (until the date come), etc
 *
 *                     Goals: [
 *                         ...
 *                     ]
 *                     state
 *                     comment
 *                 ]
 *
 *                 Projects: [
 *                     priorityLevel: 1-10
 *                     state
 *                     comment
 *                     author
 *                     whatAffects: (what change if you do project)
 *                     relation: (with what projects or something else is this project connected)
 *                     benefit:
 *                     harm:
 *                     // sum from tasks
 *                     costs: [
 *                         money:
 *                         time
 *                     ]
 *
 *                     // some tasks to do project
 *                     Tasks: [
 *                         priorityLevel: 1-10
 *                         date
 *                         name
 *                         costs: [
 *                             money
 *                             time
 *                         ]
 *                         state
 *                         comment
 *                         ...
 *                     ]
 *                 ]
 *             }
 *         ]
 *
 *
 */

