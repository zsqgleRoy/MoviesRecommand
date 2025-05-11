# Database Schema

> **Relevant source files**
> * [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

This document outlines the database schema used in the MoviesRecommend system. It provides detailed information about the tables, fields, relationships, and data types that form the foundation of the movie recommendation functionality.

For information about the broader database purpose and structure, see [Database](/zsqgleRoy/MoviesRecommand/2-database). For more detailed analysis of entity relationships and how they support application features, see [Entity Relationships](/zsqgleRoy/MoviesRecommand/2.2-entity-relationships).

## Overview

The MoviesRecommend database schema is defined in [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

 and implements a relational database model that stores and organizes information about movies, users, ratings, and related entities. This schema is structured to efficiently support core features including movie browsing, user ratings, reviews, and personalized recommendation generation.

## Database Tables Structure

The following diagram illustrates the primary tables in the database schema and their relationships:

```
#mermaid-vcqlz71paf{font-family:ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica;font-size:16px;fill:#333;}@keyframes edge-animation-frame{from{stroke-dashoffset:0;}}@keyframes dash{to{stroke-dashoffset:0;}}#mermaid-vcqlz71paf .edge-animation-slow{stroke-dasharray:9,5!important;stroke-dashoffset:900;animation:dash 50s linear infinite;stroke-linecap:round;}#mermaid-vcqlz71paf .edge-animation-fast{stroke-dasharray:9,5!important;stroke-dashoffset:900;animation:dash 20s linear infinite;stroke-linecap:round;}#mermaid-vcqlz71paf .error-icon{fill:#dddddd;}#mermaid-vcqlz71paf .error-text{fill:#222222;stroke:#222222;}#mermaid-vcqlz71paf .edge-thickness-normal{stroke-width:1px;}#mermaid-vcqlz71paf .edge-thickness-thick{stroke-width:3.5px;}#mermaid-vcqlz71paf .edge-pattern-solid{stroke-dasharray:0;}#mermaid-vcqlz71paf .edge-thickness-invisible{stroke-width:0;fill:none;}#mermaid-vcqlz71paf .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-vcqlz71paf .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-vcqlz71paf .marker{fill:#999;stroke:#999;}#mermaid-vcqlz71paf .marker.cross{stroke:#999;}#mermaid-vcqlz71paf svg{font-family:ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica;font-size:16px;}#mermaid-vcqlz71paf p{margin:0;}#mermaid-vcqlz71paf .entityBox{fill:#ffffff;stroke:#dddddd;}#mermaid-vcqlz71paf .relationshipLabelBox{fill:#dddddd;opacity:0.7;background-color:#dddddd;}#mermaid-vcqlz71paf .relationshipLabelBox rect{opacity:0.5;}#mermaid-vcqlz71paf .labelBkg{background-color:rgba(221, 221, 221, 0.5);}#mermaid-vcqlz71paf .edgeLabel .label{fill:#dddddd;font-size:14px;}#mermaid-vcqlz71paf .label{font-family:ui-sans-serif,-apple-system,system-ui,Segoe UI,Helvetica;color:#333;}#mermaid-vcqlz71paf .edge-pattern-dashed{stroke-dasharray:8,8;}#mermaid-vcqlz71paf .node rect,#mermaid-vcqlz71paf .node circle,#mermaid-vcqlz71paf .node ellipse,#mermaid-vcqlz71paf .node polygon{fill:#ffffff;stroke:#dddddd;stroke-width:1px;}#mermaid-vcqlz71paf .relationshipLine{stroke:#999;stroke-width:1;fill:none;}#mermaid-vcqlz71paf .marker{fill:none!important;stroke:#999!important;stroke-width:1;}#mermaid-vcqlz71paf :root{--mermaid-font-family:"trebuchet ms",verdana,arial,sans-serif;}receivesgivesreceiveswritesfeaturesappears_indirected_bydirectscategorized_ascontainsMOVIESintmovie_idPKvarchartitleintrelease_yeartextdescriptionvarcharposter_urlintruntimeUSERSintuser_idPKvarcharusernamevarcharemailvarcharpassword_hashdatetimeregistration_dateRATINGSintrating_idPKintuser_idFKintmovie_idFKdecimalrating_valuedatetimetimestampREVIEWSintreview_idPKintuser_idFKintmovie_idFKtextcontentdatetimetimestampACTORSintactor_idPKvarcharnametextbioDIRECTORSintdirector_idPKvarcharnametextbioGENRESintgenre_idPKvarcharnamevarchardescriptionMOVIE_ACTORSintmovie_idFKintactor_idFKvarcharcharacter_nameMOVIE_DIRECTORSintmovie_idFKintdirector_idFKMOVIE_GENRESintmovie_idFKintgenre_idFK
```

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

## Core Tables

### Movies Table

The Movies table serves as the central table in the schema, storing comprehensive information about each film in the database.

| Column Name | Data Type | Description |
| --- | --- | --- |
| movie_id | INTEGER | Primary key identifier for each movie |
| title | VARCHAR | The title of the movie |
| release_year | INTEGER | Year the movie was released |
| description | TEXT | Synopsis or plot summary |
| poster_url | VARCHAR | Link to movie poster image |
| runtime | INTEGER | Duration of the movie in minutes |
| [Additional metadata fields] | VARIOUS | May include language, country, MPAA rating, etc. |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### Users Table

The Users table stores information about registered users of the system, supporting authentication and personalization features.

| Column Name | Data Type | Description |
| --- | --- | --- |
| user_id | INTEGER | Primary key identifier for each user |
| username | VARCHAR | User's chosen display name |
| email | VARCHAR | User's email address |
| password_hash | VARCHAR | Securely stored password hash |
| registration_date | DATETIME | When the user account was created |
| [Profile fields] | VARIOUS | May include preferences, biography, etc. |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### Ratings Table

The Ratings table stores numeric evaluations that users give to movies, forming the core data for the recommendation system.

| Column Name | Data Type | Description |
| --- | --- | --- |
| rating_id | INTEGER | Primary key identifier for each rating |
| user_id | INTEGER | Foreign key reference to the Users table |
| movie_id | INTEGER | Foreign key reference to the Movies table |
| rating_value | DECIMAL | Numeric score (likely on a 1-5 or 1-10 scale) |
| timestamp | DATETIME | When the rating was submitted |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### Reviews Table

The Reviews table stores textual evaluations that users write about movies.

| Column Name | Data Type | Description |
| --- | --- | --- |
| review_id | INTEGER | Primary key identifier for each review |
| user_id | INTEGER | Foreign key reference to the Users table |
| movie_id | INTEGER | Foreign key reference to the Movies table |
| content | TEXT | The text of the review |
| timestamp | DATETIME | When the review was submitted |
| [Additional fields] | VARIOUS | May include helpfulness votes or other metadata |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### Actors Table

The Actors table stores information about film performers.

| Column Name | Data Type | Description |
| --- | --- | --- |
| actor_id | INTEGER | Primary key identifier for each actor |
| name | VARCHAR | Actor's full name |
| bio | TEXT | Biographical information |
| [Additional fields] | VARIOUS | May include birth date, nationality, etc. |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### Directors Table

The Directors table stores information about film directors.

| Column Name | Data Type | Description |
| --- | --- | --- |
| director_id | INTEGER | Primary key identifier for each director |
| name | VARCHAR | Director's full name |
| bio | TEXT | Biographical information |
| [Additional fields] | VARIOUS | May include similar fields as the Actors table |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### Genres Table

The Genres table stores categorical information about film types.

| Column Name | Data Type | Description |
| --- | --- | --- |
| genre_id | INTEGER | Primary key identifier for each genre |
| name | VARCHAR | Genre name (e.g., "Action", "Comedy", "Drama") |
| description | VARCHAR | Explanation of the genre category |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

## Junction Tables

The database uses junction tables to implement many-to-many relationships between entities:

### MovieActors Table

Links movies to the actors who appear in them.

| Column Name | Data Type | Description |
| --- | --- | --- |
| movie_id | INTEGER | Foreign key reference to the Movies table |
| actor_id | INTEGER | Foreign key reference to the Actors table |
| character_name | VARCHAR | Character name or role description |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### MovieDirectors Table

Links movies to their directors.

| Column Name | Data Type | Description |
| --- | --- | --- |
| movie_id | INTEGER | Foreign key reference to the Movies table |
| director_id | INTEGER | Foreign key reference to the Directors table |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

### MovieGenres Table

Links movies to their genre classifications.

| Column Name | Data Type | Description |
| --- | --- | --- |
| movie_id | INTEGER | Foreign key reference to the Movies table |
| genre_id | INTEGER | Foreign key reference to the Genres table |

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

## Data Flow in the Recommendation System

The following diagram illustrates how data flows through the database during the movie recommendation process:

```

```

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

## Database Constraints and Indexing

The database schema implements several important constraints and indexing strategies to maintain data integrity and query performance:

1. **Primary Keys**: Each table has a primary key constraint to ensure unique identification of records
2. **Foreign Keys**: Tables maintain referential integrity through foreign key constraints
3. **Indexes**: Strategic indexing on frequently queried columns improves search performance
4. **NOT NULL constraints**: Essential fields are marked as NOT NULL to ensure data completeness
5. **Unique constraints**: Applied to fields like usernames and email addresses to prevent duplicates

## Schema Support for Core Features

The database schema efficiently supports the core features of the movie recommendation system:

### User Management and Authentication

The Users table provides the foundation for user account management and authentication as detailed in [Authentication](/zsqgleRoy/MoviesRecommand/4.1-authentication), storing credentials and profile information securely.

### Movie Exploration

The Movies table and its relationships with Actors, Directors, and Genres tables enable the comprehensive browsing and search functionality described in [Movie Browsing](/zsqgleRoy/MoviesRecommand/4.2-movie-browsing). This allows users to discover films based on various criteria.

### Rating and Review System

The Ratings and Reviews tables link users to movies and store their evaluations, supporting the functionality outlined in [Movie Rating](/zsqgleRoy/MoviesRecommand/4.3-movie-rating). These tables capture both quantitative (numeric ratings) and qualitative (text reviews) user feedback.

### Recommendation Algorithm

The schema is optimized for the recommendation functionality described in [Recommendations](/zsqgleRoy/MoviesRecommand/4.4-recommendations). By analyzing data from the Ratings table along with movie metadata from related tables, the system can identify patterns and generate personalized movie suggestions.

## Implementation Notes

The complete database schema is defined in [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)

 This file is tracked using Git LFS due to its large size (approximately 631MB), which indicates it likely contains not only the schema definition but also substantial movie data and possibly initialization scripts.

The database size suggests a comprehensive collection of movie information, including:

* A large catalog of movies with detailed metadata
* Extensive information about actors and directors
* A comprehensive genre classification system
* Potentially sample user data and ratings for testing

Sources: [movie_db.sql](https://github.com/zsqgleRoy/MoviesRecommand/blob/49b41f2a/movie_db.sql)