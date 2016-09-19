# FixTheFixing
Fix the Fixing EU project code

###Before you run:
1) Install Mongo in Windows: https://www.youtube.com/watch?v=1uFY60CESlM <br/>
2) Install MongoDB plugin in IntelliJ <br/>
3) Install Maven in Windows: http://www.mkyong.com/maven/how-to-install-maven-in-windows/ <br/>
4) Check out project from Version Control <br/>
5) Create mongod service in Windows <br/>

###Dependencies
org.mongodb:mongodb-driver:3.2.2

###Folder Creation
Folder "out" should be created manually. In order to run sentiment analysis a subfolder with the name of the study case should be created. e.g. "out/djokovic"

###For WordNet:

  - Download WordNet [executable](http://wordnet.princeton.edu/wordnet/download/current-version/) *(not just DB files)*

  - Download [rita.jar](http://www.rednoise.org/rita/download.php) *(end of page)*

  - Add rita.jar to project and if needed change path in getSynonyms function (line: RiWordNet wordnet = new RiWordNet("C:\\Program Files (x86)\\WordNet\\2.1"); // The WordNet directory)

###Example Arguments for Twitter
querysearch="(Djokovic OR Novak) fixing" since=2016-01-18 until=2016-01-30 maxtweets=1000 theme="djokovic"

###Search query for Mongo Explorer (Ctrl+F in IntelliJ)
For instance, if I'm looking for a tweet created at 2016/01/28 I'd type
in the filter text box: {'tweet.date':'2016/01/28'} , which typically follows a JSON format.

###Credentials for Youtube
In order to use the Google API and collect Youtube comments, each user needs Google Credentials  <br/> 
More: <https://developers.google.com/youtube/registering_an_application#Create_API_Keys>

The API key should be inserted in the first line of [~Credentials/Youtube/API_KEY.txt] file
