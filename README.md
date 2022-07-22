# Locale

## Overview
### Description
Going someplace new? Locale is a travel app to allow individuals new to an area to explore and visit popular landmarks. Locale generates a custom list of landmarks to visit based on user interests and intensity (how many landmarks they want to visit). The app marks locations as visited in real time, prompting users to rate the location, leave a review, and upload an image from their trip after detecting that a user is in a location on their list of locations to visit! Users can see where locations are on a map and view a timeline of their travels on their profile page. No network connection? Locale has limited offline access that allows users to view their list of locations to visit as well as the locations they've visited in the past! Haven't explored someplace new in a while? Locale sends push notifications to users if they have never visited someplace on their list or if they haven't visited someplace new in a couple of days. Locale will also recommend new places to visit based on your current location, so you can always find someplace new to explore!

### App Evaluation
- **Category:** Travel
- **Mobile:** Mobile is essential for travel. The mobile plaform enables real time detection of user's locations, the upload of user photos, and the reminder notifications for users to explore new places.
- **Story:** Generates a list of local landmarks based on a user's location. The user can "checks off" locations by physically visiting the location or manually marking  locations as visited. 
- **Market:** Any individual who is traveling and wants to explore someplace new could utilize this app.
- **Habit:** Individuals interested in going someplace new can check the app to see what locations are recommended/nearby. Users can also check photos from the day they visited (if they wanted to see photos that other users took) and review places they visited.
- **Scope:** In the first version, users can view a list of locations and mark locations as visited. The second version would incorporate details about each location (photos). Version three would include a way to rate the location add additional information/reviews/photos for each of the locations. Version four would incorporate a timeline for people to revisit their travels.


## Product Spec
### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Users log in to access personalized information about their location
* Users log out from their account on their profile page
* App finds the user’s current location
* Users can mark locations as visited
* Generates list of nearby locations for the user to visit (filtered by interests)
* Users input their interests to better filter landmarks
* App caches locations recommended to the user, whether or not they’ve been visited, and location descriptions


**Optional Nice-to-have Stories**
* Users can view their travel history on their profile page
* Users can upload images/reviews of a location after they have visited the location
* Users can customize their experience (additional user properties like what kind of traveler they are)
* App detects the user being in a new location and asks if they want to add new landmarks for that area to their list to visit
* Explore page to view activity of other users around the globe

### 2. Screen Archetypes

* Login: User signs into their account
* Register: User creates their account
   * Upon Download/Reopening of the application, the user is prompted to log in to gain access to their profile information
* Home Screen
    * Allows users to view recommended landmarks and their list of locations to visit
* Interests Screen
    * Allows users to customize their interests and how many locations are shown
* Discover Screen
    * Allows uers to view activity of other users
* Map Screen
    * Allows users to view pins of where locations are on a map
* Profile Screen 
   * Allows user to view their previously visited locations


### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home
* Map
* Profile

Optional:
* Discover (User posts)

**Flow Navigation** (Screen to Screen)
* Forced Log-in -> Automatically logs in the user if possible; otherwise, users can log in/create a new account
* Home -> Review location visited pop-up dialog
* Profile -> Returns to log in page on logout button click

## Wireframes
<img src="" width=800><br>

## Schema 
### Models
#### User

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
| username       | String   | unique username for the user (default field)|
   | first_name        | String   |  first name of the user |
   | last_name         | String   | last name of the user |
   | password | String   | password for the user's account |
   | location    | GeoPoint   | the location where the user created their account |
   | not_visited_landmarks     | Array, each element is a pointer to a Location | stores all the user's locations to visit|
   |visited_landmarks | Array, each element is a JSON Object | stores all the user's visited locations and information about each location (the date the location was visited, and a user uploaded photo) |
   | interests     | Array of Strings| stores a list of user interests |
   | pace | Integer | value of 5, 10, or 20 depending on how many locations the user wants to visit |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
   
#### Location

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user (default field) |
   | coordinates       | GeoPoint   |  the latitude and longitude coordinates for a location |
   | place_name         | String   | name of the location |
   | vicinity | String   | the location address |
   | types    | Array   | stores the types of categories the location falls under |
   | times_visited     | Integer | stores the number of times a location has been visited|
   | total_rating | Double | stores the total rating of a location given by users |
   | review     | Array of Strings| stores a list of reviews given by users |
   | photos | Array of Strings | stores all user uploaded photos of a location |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
   
   
   
### Networking
#### List of network requests by screen
   - Home Feed Screen
      - (Read/GET) Query logged in user object
      - (Read/GET) Query all list of user's location to visit
      - (Read/GET) Query nearby locations to the user's current location
      - (Update/PUT) Update user's locations to visit when a recommended landmark is added to the list of locations to visit
      - (Update/PUT) Update user's locations to visit and visited locations when a location is marked as visit
      - (Create/POST) Create a post when users mark a location visited
      - (Create/POST) Create a new post object
   - Discover Screen
      - (Read/GET) Query posts from Parse
   - Maps Screen
      - (Read/GET) Query user's locations to visit and display them as pins on a map
   - Profile Screen
      - (Read/GET) Query logged in user object
