import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class logExtractor_noAuth {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/conversation_logs";

    // Database credentials
    static final String USER = "logger";
    static final String PASS = "logger#123";

    /**
     ===============================================================
     This method invokes the bots API and returns the extracted logs
     ===============================================================
     **/
    public static JsonObject apiCaller() {

        // Declaring all necessary parameters before attempting their definition in the try-catch block
        URL url = null;
        HttpURLConnection urlhc = null;
        JsonObject extractedLog = null;

        // Try invoking the bots API
        try {
            url = new URL("<Oracle Digital Assistant bot URL>" + current_bot_id + "conversationMessages?since=2019-01-17T00%3A00%3A00.007Z&format=json");
            urlhc = (HttpURLConnection) url.openConnection();
            urlhc.setRequestMethod("GET");
            if (urlhc.getResponseCode() != 200) {
                throw new RuntimeException("Failed: HTTP error code: " + urlhc.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(urlhc.getInputStream()));
            BufferedInputStream bis = new BufferedInputStream(urlhc.getInputStream());      //for Byte[]

            System.out.println("Output from server....");
            String output;

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) urlhc.getContent()));
            extractedLog = root.getAsJsonObject();
            //System.out.println("extractedLog: " + extractedLog);

        } catch (Exception e) {
            System.out.println(e);
        }

        //return extractedLog to main();
        return extractedLog;
    }

    /*
        =========================================================================
        Method to push the extracted logs, on log at a time to the local MySQL DB
        =========================================================================
    */
    public static void dbpush(JsonObject extractedLog) {
        //System.out.println("extractedLog received: " + extractedLog);

        // Declaring necessary connection parameters for local DB
        Connection conn = null;
        Statement stmt = null;

        // Attempt to push to DB
        try {
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);     // Get connection using the global variables defined earlier
            System.out.println("SUCCESS!");

            // The JSON response from the API call returns a single JSONArray named "items" comprising all logs
            // We retrieve this array first, then parse it one log at a time in the for() loop just below
            JsonArray response;
            response = extractedLog.getAsJsonArray("items"); //returns the array of all logs

            // Parse through each log in the JSONArray response
            for (int i = 0; i < response.size(); i++) {
            //for (int i = 0; i < 11; i++) {

                // Obtain the log as a JsonObject so we can use the JsonObject.get("string id") method to retrieve individual parameters to push into the DB
                JsonObject temp = response.get(i).getAsJsonObject();
                System.out.println("temp: " + temp);

                String text_id ="";     // The unique ID for each individual log, thus acts as the primary key in our logs DB
                if (!temp.get("id").isJsonNull())
                    text_id = temp.get("id").getAsString();
                System.out.println("text_id: " + text_id);

                String text = "";
                if (!temp.get("text").isJsonNull())
                    text = temp.get("text").getAsString();
                System.out.println("text: " + text);

                String choices ="";
                if (!temp.get("choices").isJsonNull())
                    choices = temp.get("choices").getAsString();
                System.out.println("choices: " + choices);

                String source ="";
                if (!temp.get("source").isJsonNull())
                    source = temp.get("source").getAsString();
                System.out.println("source: " + source);


                String createdOn ="";
                if (!temp.get("createdOn").isJsonNull())
                    createdOn = temp.get("createdOn").getAsString();
                System.out.println("createdOn: " + createdOn);

                // CreatedOn comprises of the dateTime in the format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
                // To parse this, we create an appropriate Java SimpleDateFormat object
                // This is done to later obtain the appropriate date and time for sql insertion via java.sql
                SimpleDateFormat createdOn_dateTime_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);

                // Parsing using the above format
                Date createdOn_dateTime = createdOn_dateTime_format.parse(createdOn);
                System.out.println("createdOn_dateTime: " + createdOn_dateTime);

                // Finally, obtaining the chat date and time ready for insertion into the SQL DBs date and time fields
                java.sql.Date createdDate_sql = new java.sql.Date(createdOn_dateTime.getTime());
                java.sql.Time createdTime_sql = new java.sql.Time(createdOn_dateTime.getTime());
                System.out.println("createdDate_sql: " + createdDate_sql);
                System.out.println("createdTime_sql: " + createdTime_sql);


                // Obtaining the current time and date to log the DB insertion in the tables inserted_date and inserted_time fields
                String push_date = java.time.LocalDate.now().toString();
                String push_time = java.time.LocalTime.now().toString();

                System.out.println("push_date: " + push_date);
                System.out.println("push_time: " + push_time);

                // Again, defining appropriae formats for the data and time
                SimpleDateFormat pushDate_fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                SimpleDateFormat pushTime_fmt = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

                // Parsing using the above format
                Date pushDate = pushDate_fmt.parse(push_date);
                Date pushTime = pushTime_fmt.parse(push_time);

                // Finally, obtaining java.sql date and time objects for DB insertion into time and date fields respectively
                java.sql.Date pushDate_sql = new java.sql.Date(pushDate.getTime());
                java.sql.Time pushTime_sql = new java.sql.Time(pushTime.getTime());

                System.out.println("pushDate_sql: " + pushDate_sql);
                System.out.println("pushTime_sql: " + pushTime_sql);


                // channelConv is an array which is part of each log, thus its a nested array for each log entry in the "items" array
                // Accordingly, we once again parse it as a JSON Object, just as we did for the entire log entry at the start of this for loop
                // This lets us obtain it's members in the key:value JSON object format, so again we can retrive them using .get()
                JsonObject channelConv = temp.get("channelConversation").getAsJsonObject();
                String botId = "";
                if (!channelConv.get("botId").isJsonNull())
                    botId = channelConv.get("botId").getAsString();
                System.out.println("botId: " + botId);

                String channelId = "";
                if (!channelConv.get("channelId").isJsonNull())
                    channelId = channelConv.get("channelId").getAsString();
                System.out.println("channelId: " + channelId);

                String userId = "";
                if (!channelConv.get("userId").isJsonNull())
                    userId = channelConv.get("userId").getAsString();
                System.out.println("userId: " + userId);

                String sessionId = "";
                if (!channelConv.get("sessionId").isJsonNull())
                    sessionId = channelConv.get("sessionId").getAsString();
                System.out.println("sessionId: " + sessionId);

                String channelType = "";
                if (!channelConv.get("type").isJsonNull())
                    channelType = channelConv.get("type").getAsString();
                System.out.println("channelType: " + channelType);
                // End of nested channelConv array


                // Evaluating the choices and text fields obtained earlier
                String payload ="";
                String ratingStr = "";
                int rating = 0;

                if (source.equals("BOT")) {
                    if (!choices.isEmpty())
                        payload = text + " " + choices;     // When the log originated from the bot, the payload is either a combination of the text field and corresponding choices for the user to select from (if choices were presented)...
                    else
                        payload = text;                     // ...or simply the text that the bot displayed
                }
                else if (source.equals("USER")) {
                    if (!text.isEmpty())
                        payload = text;      // When the log originated from the user and the 'text' field isn't blank, the payload comprises of whatever is in this 'text' field, i.e. whatever the user themselves typed...
                    else {                   // ...otherwise it'll comprise of the action selected by the user from the list of options presented by the bot
                        JsonObject payload_temp = temp.getAsJsonObject("payload");
                        JsonObject postback_array = payload_temp.getAsJsonObject("postback");
                        if (postback_array.has("action"))
                            payload = postback_array.get("action").toString();
                        else
                            payload = "";    // A blank payload simply indicated that the user clicked to expand a presented option from a list of possible answers the bot found to the users question
                        // that specific expansion will be returned in the immediate next log which will be from the bot and comprise the full expanded answer
                        if (postback_array.has("variables")) {
                            JsonObject variables_obj = postback_array.getAsJsonObject("variables");
                            if (variables_obj.has("SelectedRating") && !variables_obj.get("SelectedRating").isJsonNull()) {
                                ratingStr = variables_obj.get("SelectedRating").toString();
                                ratingStr = ratingStr.replaceAll("\"","");
                                rating = Integer.parseInt(ratingStr);
                                System.out.println("rating: " + rating);
                            }
                        }
                    }
                }
                System.out.println("payload: " + payload);

                // All necessary parameters obtained, begin the DB push attempt by defining a java.SQL PreparedStatement object and setting its parameters:
                System.out.println("Inserting into conversation_logs database...");
                String sql = "INSERT IGNORE into logs (chat_date, chat_time, inserted_date, inserted_time, text, choices, source, userId, sessionId, botId, channelType, channelId, payload, text_id, rating)" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setDate(1, createdDate_sql);
                preparedStatement.setTime(2, createdTime_sql);
                preparedStatement.setDate(3, pushDate_sql);
                preparedStatement.setTime(4, pushTime_sql);
                preparedStatement.setString(5, text);
                preparedStatement.setString(6, choices);
                preparedStatement.setString(7, source);
                preparedStatement.setString(8, userId);
                preparedStatement.setString(9, sessionId);
                preparedStatement.setString(10, botId);
                preparedStatement.setString(11, channelType);
                preparedStatement.setString(12, channelId);
                preparedStatement.setString(13,payload);
                preparedStatement.setString(14, text_id);

                if (rating == 0)
                    preparedStatement.setNull(15, java.sql.Types.INTEGER);
                else
                    preparedStatement.setInt(15, rating);

                preparedStatement.execute();
                System.out.println("SUCCESS!");
                TimeUnit.SECONDS.sleep(1);
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        ===========
        main method
        ===========
    */
    public static void main(String args[]) {

        // 1 - Obtain the conversation logs from the apiCaller() method:
        JsonObject extractedLog = apiCaller();

        System.out.println("extractedLog: " + extractedLog);

        // 2 - Push the logs to the local DB by passing the logs obtained above to the dbpush() method:
        dbpush(extractedLog);

    }
}
