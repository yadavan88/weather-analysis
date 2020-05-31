## WEATHER KPIs USING AKKA PERSISTENCE ##
This is a sample application to build weather kpis using _scala_, _akka-http_, _akka-persistence_. The temperature data is passed to the akka actor, which persists the events to levelDB. After persisting, it creates DAILY and WEEKLY temperature kpis for Min_Temp_Of_Week, Max_Temp_Of_Week, Min_Temp_Of_Day, Max_Temp_Of_Day.
As and when new events are arrived, the relevant kpi will be automatically updated.

These pre-calculated states can be queried using a simple REST Service, which is implemented using Akka-http.

#### Urls: ####

All services are POST methods, with payload json format similar to {"date":"2020-05-17"}

http://localhost:9000/weatherApp/temperature/weeklyMin
http://localhost:9000/weatherApp/temperature/weeklyMax
http://localhost:9000/weatherApp/temperature/dailyMax
http://localhost:9000/weatherApp/temperature/dailyMin

Spray-Json libary is used for JSON Parsing.

If for the selected date, there is no temperature history available, then the service will return *204 No-Content* response.

There is also a akka scheduler scheduler running at specified intervals, which reads  the temperature csv files from *dataDir* directoy. For any files present there, it will read and publish the data to the temperature processor actor.

The akka-persistence snapshots will be taken at regular intervals, using another akka schduler, which is initialized on boot of the application.

The tests are available for REST Service as well as Persistent actors. Tests using in-memory persistence, which is configured in test/application.conf


Sample data for testing is downloaded from below url:

https://www.meteoblue.com/en/weather/archive/export/india_el-salvador_3585481
