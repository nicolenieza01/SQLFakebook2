package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {

                    FirstNameInfo info = new FirstNameInfo();

                    // shortest first names in alphabetical order 
                    ResultSet rst = stmt.executeQuery("select distinct first_name " + "from" + " " + UsersTable + " "+
                    "where length(first_name) = select min(length(first_name)) from" + " " + UsersTable + ")" + " " +
                     "order by first_name asc");
                     // adding the shortest name now 


                     while (rst.next()){
                        info.addShortName(rst.getString(1));

                     }



// retriving the first name info first, longest names returned in alphabetical order 

                    rst = stmt.executeQuery("select distinct first_name" + " "+ "from" + UsersTable + " " +
                    "where length(first_name) = select max(length(first_name)) from"+ " " + UsersTable + ") " +
                     "order by first_name asc");


                    while (rst.next()){
                        info.addLongName(rst.getString(1));

                     }
                     /*COUNT(*) Number of records in the table regardless of NULL values and duplicates. */
                    /* we want the name with the highest count as well as the number of occurances of this name, thus pulling first_name and count(*)*/
                    rst = stmt.executeQuery("select distinct first_name, count(*)" + " " + "from"+ " " + UsersTable + " " + "group by first_name" + " "
                    /* singling down to only first names */
                    + "having count(*) = select max(count(*)) from" + " "
                    /* selecting the count with highest number of occurences */
                     + UsersTable + "order by first_name asc)");


                    while (rst.next()){
                        info.addCommonName(rst.getString(1));
                        info.setCommonNameCount(rst.getInt(2));
                    }
                    rst.close();
                    stmt.close();




            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            rst.close();
            stmt.close(); 
            return new FirstNameInfo(); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {


            ResultSet rst = stmt.executeQuery( "select user_id, first_name, last_name" + " " + "from" + " " + UsersTable

            + " " + "where user_id not in (select distinct user1_id from" + " " + FriendsTable
            /* must be distinct so that users arent accounted for twice */
             + " " + "union select distinct user2_id" + " " + "from" + " " + FriendsTable
             /*union =  combine the result-set of two or more SELECT statements.*/
            
            + ")" + "order by user_id asc");
            /* order alphabetically on return */



            while (rst.next()){
                UserInfo u1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                results.add(u1);
            }


            rst.close();
            stmt.close(); 



            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {


                    ResultSet rst = stmt.executeQuery("select u.user_id, u.first_name, u.last_name" +" "
                     + "from" + " " + UsersTable + " " + "u," + " " + CurrentCitiesTable + " " + "c," + " " + HometownCitiesTable + " " +  "h" + " " + 
                     "where u.user_id = c.user_id" + " " + "and u.user_id = h.user_id" + " " + "and c.current_city_id != h.hometown_city_id"+ " " + "order by u.user_id asc");

                    /*simple just want ids to match up and current city id to not = hometown city id */
                    while (rst.next()){
                UserInfo u1 = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
                results.add(u1);
            }


            rst.close();
            stmt.close(); 







            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

         try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {


                    ResultSet rst = stmt.executeQuery( 
                            "SELECT * FROM ( SELECT P1.photo_id, A.album_id, P1.photo_link, A.album_name, " +
                            "(SELECT COUNT (*) FROM " + PhotosTable + " P2 JOIN " + TagsTable + " T ON P2.photo_id = T.tag_photo_id WHERE P2.photo_id = P1.photo_id) as tagged_users " +
                            "FROM " + PhotosTable + " P1 " +
                            " JOIN " + AlbumsTable + " A ON P1.album_id = A.album_id " +
                            "ORDER BY tagged_users DESC, P1.photo_id ASC) " +
                            "WHERE ROWNUM <= " + num);
                    while (rst.next()){
                        
                        long pID = rst.getLong(1);
                        long aID = rst.getLong(2);
                        String link = rst.getString(3);
                        String aName = rst.getString(4);
                        PhotoInfo p = new PhotoInfo(pID, aID, link, aName);
                        TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                        try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                            FakebookOracleConstants.ReadOnly)) {
                                ResultSet rst2 = stmt2.executeQuery(" SELECT U.user_id, U.first_name, U.last_name FROM " + UsersTable + " U JOIN " + TagsTable + 
                            " T ON T.tag_subject_id = U.user_id WHERE T.tag_photo_id = " + pID + " ORDER BY U.user_id");

                            while(rst2.next()){
                                UserInfo u = new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3));
                                tp.addTaggedUser(u);
                            }
                            rst2.close();
                            stmt2.close();
                        }
                        catch (SQLException e) {
                            System.err.println(e.getMessage());
                        }
                        results.add(tp);
                    }
            
            rst.close();
            stmt.close(); /*Added in case ag doesn't run*/
            return results;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

         try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                            "SELECT * FROM (" +
                                "SELECT U1.user_id, U1.first_name, U1.last_name, U1.year_of_birth," +
                                "U2.user_id, U2.first_name, U2.last_name, U2.year_of_birth, " +
                                "(SELECT COUNT(*) FROM " + PhotosTable + " P, " + TagsTable + " T1, " + TagsTable + " T2 " + 
                                    "WHERE T1.tag_photo_id = P.tag_photo_id " + 
                                    "AND T2.tag_photo_id = P.tag_photo_id" + 
                                    "AND T1.tag_subject_id = U1.user_id " + 
                                    "AND T2.tag_subject_id = U2.user_id) as photos_together " +
                                "FROM " + UsersTable + " U1, " + UsersTable + " U2 " +
                                "WHERE U1.gender = U2.gender " + 
                                "AND ABS(U1.year_of_birth - U2.year_of_birth) <= " + yearDiff + 
                                " AND U1.user_id < U2.user_id  " +
                                "AND NOT EXISTS (" + 
                                    "SELECT * FROM " + FriendsTable + " F " +
                                     "WHERE F.user1_id = U1.user_id " + 
                                    "AND F.user2_id = U2.user_id)" +
                                " ORDER BY photos_together DESC, U1.user_id ASC, U2.user_id ASC) " +
                            "WHERE ROWNUM <= " + num + "");
                                
                    while (rst.next()){
                        long uId1 = rst.getLong(1);
                        long uId2 = rst.getLong(5);
                        UserInfo u1 = new UserInfo(uId1, rst.getString(2), rst.getString(3));
                        UserInfo u2 = new UserInfo(uId2, rst.getString(6), rst.getString(7));
                        MatchPair mp = new MatchPair(u1, rst.getLong(4), u2, rst.getLong(8));
                        try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                            FakebookOracleConstants.ReadOnly)) {
                                ResultSet rst2 = stmt2.executeQuery(
                                "SELECT P.photo_id, P.photo_link, A.album_id, A.album_name FROM " + AlbumsTable + "A" +
                                "JOIN " + PhotosTable + " P ON A.album_id = P.album_id " + 
                                "JOIN " + TagsTable + " T1 ON T1.tag_photo_id = P.tag_photo_id " +
                                "JOIN " + TagsTable + " T2 ON T2.tag_photo_id = P.tag_photo_id " + 
                                "WHERE T1.tag_subject_id = " + uId1 +  
                                " AND T2.tag_subject_id = " + uId2 + " ORDER BY P.photo_id");

                            while(rst2.next()){
                                PhotoInfo p = new PhotoInfo(rst2.getLong(1), rst2.getLong(2), rst2.getString(3), rst2.getString(4));
                                mp.addSharedPhoto(p);
                            }
                            results.add(mp);
                            rst2.close();
                            stmt2.close();
                        }
                        catch (SQLException e) {
                            System.err.println(e.getMessage());
                        }
                    }
            
            rst.close();
            stmt.close(); /*Added in case ag doesn't run*/
            return results;
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
            stmt.executeUpdate("create view friendship " + "as " + "select f1.user1_id as user1_id, f1.user2_id as user2_id" + " " + "from" + " " + FriendsTable + " f1 " + "union" + " select f2.user1_id as user1_id, f2.user2_id as user2_id" +
                    " from " + FriendsTable + " f2 ");

            ResultSet rst = stmt.executeQuery(
                            "SELECT * FROM (" +
                                "SELECT U1.user_id, U1.first_name, U1.last_name, " +
                                "U2.user_id, U2.first_name, U2.last_name, " +
                                "(SELECT COUNT(*) FROM Users U3, friendship F1, friendship F2 " + 
                                    "WHERE U3.user_id = F1.user1_id " + 
                                    "AND U3.user_id = F2.user1_id " + 
                                    "AND U1.user_id = F1.user2_id " + 
                                    "AND U2.user_id = F2.user2_id " +
                                    "AND U1.user_id > U2.user_id) as friends_together " +
                                "FROM " + UsersTable + " U1, " + UsersTable + " U2 " +
                                "WHERE U1.user_id < U2.user_id  " +
                                "AND NOT EXISTS (" + 
                                    "SELECT * FROM " + FriendsTable + " F " +
                                     "WHERE F.user1_id = U1.user_id " + 
                                    "AND F.user2_id = U2.user_id)" +
                                " ORDER BY friends_together DESC, U1.user_id ASC, U2.user_id ASC) " +
                            "WHERE ROWNUM <= " + num + "");
                                
                    while (rst.next()){
                        long uid1 = rst.getLong(1);
                        long uid2 = rst.getLong(4);
                        UserInfo u1 = new UserInfo(uid1, rst.getString(2), rst.getString(3));
                        UserInfo u2 = new UserInfo(uid2, rst.getString(5), rst.getString(6));
                        UsersPair up = new UsersPair(u1, u2);
                        try (Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll,
                            FakebookOracleConstants.ReadOnly)) {
                                ResultSet rst2 = stmt2.executeQuery("SELECT U.user_id, U.first_name, U.last_name FROM Users U, friendship F1, friendship F2 " + 
                                    "WHERE U.user_id = F1.user1_id " + 
                                    "AND U.user_id = F2.user1_id " + 
                                    "AND " + uid1 + " = F1.user2_id " + 
                                    "AND " + uid2 + "U2.user_id = F2.user2_id " +
                                    "ORDER BY U.user_id ASC");

                            while(rst2.next()){
                                UserInfo u3 = new UserInfo(rst2.getLong(1), rst2.getString(2), rst2.getString(3));
                                up.addSharedFriend(u3);
                            }
                            results.add(up);
                            rst2.close();
                            stmt2.close();
                        }
                        catch (SQLException e) {
                            System.err.println(e.getMessage());
                        }
                    }
            
            stmt.executeUpdate("drop view friendship");
            rst.close();
            stmt.close(); /*Added in case ag doesn't run*/
            return results;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {


                    int nicole = 0;
                    String common = "";
                    ResultSet rst = stmt.executeQuery("select count(*), state_name" + " " + "from" + " " + EventsTable + " " + "e," + " " + CitiesTable + " c"
                    + " " + "where e.event_city = c.city_name" + " group by state_name" + " " + "having count(*) = (select(max(count(*)) from" + " " + 
                    EventsTable + " " + "e," + " " + CitiesTable + " c" + " " + "where e.event_city = c.city_name" + " " + "group by state_name)");




                    while (rst.next()) {
                        nicole = rst.getInt(2);
                        common = rst.getString(1);
                        EventStateInfo info = new EventStateInfo(nicole);
                        info.addState(common);


                    }


            rst.close();
            stmt.close();



            

            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
            return new EventStateInfo(-1); // placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */
            
            ResultSet rst = stmt.executeQuery(
                            "SELECT u_id, u_fname, u_lname FROM (" +
                            "SELECT F1.user2_id AS u_id, U1.first_name as u_fname, U1.last_name AS u_lname, U1.year_of_birth, U1.month_of_birth, U1.day_of_birth " + 
                            "FROM " + FriendsTable + " F1 " +
                            "JOIN " + UsersTable + " U1 ON F1.user2_id = U1.user_id " + 
                            "WHERE F1.user1_id = " + userID  + " " +
                            "UNION " +
                            "SELECT F2.user1_id AS u_id, U2.first_name AS u_fname, U2.last_name AS u_lname, U2.year_of_birth, U2.month_of_birth, U2.day_of_birth " + 
                            "FROM " + FriendsTable + " F2 " +
                            "JOIN " + UsersTable + " U2 ON F2.user1_id = U2.user_id " +
                            "WHERE F2.user2_id = " + userID  + " ) results " +
                            "ORDER BY year_of_birth ASC, month_of_birth ASC, day_of_birth ASC, u_id DESC");

            int oldestID = 0;
            String oldestFirst = "";
            String oldestLast = "";




            int youngestID = 0;
            String youngestFirst = "";
            String youngestLast = "";
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    oldestID = rst.getInt(1); //   it is the month with the most
                    oldestFirst = rst.getString(2);
                    oldestLast = rst.getString(3);
                }
                if (rst.isLast()) { // if last record
                    youngestID = rst.getInt(1); //   it is the month with the least
                    youngestFirst = rst.getString(2);
                    youngestLast = rst.getString(3);
                }
            }

            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically
            return new AgeInfo(new UserInfo(oldestID, oldestFirst, oldestLast), new UserInfo(youngestID, youngestFirst, youngestLast));// placeholder for compilation
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
           

            // Step 1
            // ------------
            // * Find all users who are friends
            ResultSet rst = stmt.executeQuery(
                        "SELECT U1.user_id, U1.first_name, U1.last_name, U2.user_id, U2.first_name, U2.last_name, H1.hometown_city_id, U1.year_of_birth, H2.hometown_city_id, U2.year_of_birth " + // select birth months and number of uses with that birth month
                            "FROM " + FriendsTable + " F " +
                            "JOIN " + UsersTable + " U1 ON U1.user_id = F.user1_id " +
                            "JOIN " + UsersTable + " U2  ON U2.user_id = F.user2_id " +
                            "JOIN " + HometownCitiesTable + " H1 ON U1.user_id = H1.user_id " + 
                            "JOIN " + HometownCitiesTable + " H2 ON U2.user_id = H2.user_id " + 
                            "WHERE U1.last_name = U2.last_name AND H1.hometown_city_id = H2.hometown_city_id AND ABS(U1.year_of_birth - U2.year_of_birth) < 10 AND U1.user_id < U2.user_id " +
                            "ORDER BY U1.user_id, U2.user_id");

            while (rst.next()) {
                UserInfo u1 = new UserInfo(rst.getInt(1), rst.getString(2), rst.getString(3));
                UserInfo u2 = new UserInfo(rst.getInt(4), rst.getString(5), rst.getString(6));
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
