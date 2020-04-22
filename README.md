# Lingvino

[![CircleCI](https://circleci.com/gh/cecobask/Lingvino/tree/master.svg?style=svg)](https://circleci.com/gh/cecobask/Lingvino/tree/master)


## Project Setup
1. Clone the repository
2. Open the project in Android Studio
3. Run the application


## Application Features

<details>
    <summary>Multilingual user interface</summary>
    <p>
    The Android application is designed in such a way that enables the user interface language to be
    changed by simply performing a few clicks. This is done through visiting the “Language Options”
    control panel. However, when a user registers and is using the application for the first time, they will
    be required to set their language preferences before being able to access any application functionality.
    There are two options the user has to select – spoken language (source) and a language they would
    like to learn (target). The Android application supports four languages – English, Bulgarian, Russian
    and Spanish. When the user selects their spoken language, the list of target languages updates by
    excluding the spoken language in order to prevent users from choosing the same language as source
    and target. Once the user decides what their language preferences are, they will be redirected to the
    Dashboard screen of the application. The user interface will reflect their language preferences, thus
    making sure they understand all the content they are about to come across. There is a translation
    mapped to every menu option and user interface control, corresponding to the chosen source and
    target languages. The translations are stored as strings in the default string resources file for Android
    projects, where they can be accessed by every fragment and activity in the application. The language
    options can be updated at any point the user wants to. Navigating to the “Language Options” is easy,
    as the user interface offers multiple ways to do it. The first way is to use the navigation drawer and
    select the appropriate menu entry. The second way is by visiting the Dashboard screen where the user
    can see the main points of interest in the application. The dashboard provides a quick way to access
    these points, one of the CardView elements presented there is the “Language Options”. The web
    application only supports English language.
    </p>
</details>


<details>
    <summary>Word of the day</summary>
    <p markdown="1">
    Word of the day is one of the main features of the Android application. Its aim is to teach the Android
    application users a new word every day. It comprises of the following:
    * The word itself – a single word that is translated to match the target language of the user. The
    core word is the same for every user, no matter what their location or language preferences
    are.
    * Word transliteration – the word converted into a readable and easy to understand format. It
    involves swapping each letter in the word with a letter or combination of letters from the
    user’s spoken language. Its aim is to help the users learn how to read and pronounce the word
    in a language that they do not understand. This functionality was achieved by carefully
    researching the linguistics of the four languages that the application supports. Finally, a
    function was implemented that constructs the resulting transliteration by replacing the
    letters.
    * Word pronunciation – an audio recording of the word, pronounced by a computerised
    speaker. This was achieved by using a third-party API from Microsoft Azure – Cognitive
    Services. It requires text input and language options parameters to be send via HTTP request.
    The result it returns is the audio file with pronunciation. This file gets stored to Google Storage
    bucket, a reference to it is obtained and saved to the WordOfTheDay object. In this way I
    minimise the charges my API calls incur by storing the results, instead of repeatedly requesting
    the same data.
    * Word type – every word belongs to a certain category (part of speech). Possible values are
    noun, verb, adjective, adverb, etc.
    * Word definition – this is the meaning of the word. It is usually one or two sentences long. It is
    retrieved from a third-party API – Wordnik. Before saving, the results from the API calls are
    being parsed to strip html tags and unnecessary symbols. The word definition is translated in
    the user’s source language.
    * Example sentence – a sentence that contains the current word of the day. Its aim is to give a
    context to the user of how the word could be used in a sentence and possibly suggest
    examples that they can use in real life. This data is also retrieved from the Wordnik API. By
    default, the example sentence is translated to the user’s target language. However, the
    Android application interface allows users to click on an example sentence, which in return
    would swap the example sentence with its translation in the user’s source language.
    The word of the day object is generated by a Firebase Cloud Function that is automatically triggered
    every 24 hours (Dublin time). The function retrieves a list of words that haven’t been selected before
    from the database and picks a random one. This word is then used as a parameter to invoke the
    necessary third-party APIs (Wordnik, Microsoft Azure Cognitive Services and Google Translation API).
    The results from all API calls are combined in a WordOfTheDay object that gets stored in the Firebase
    Database. The Android application would use the word of the day database collection to display the
    current word or present all previous words in a Calendar view. The user can access previous words by
    clicking on the “random word” button or using the Calendar view to access a word from a specific
    date.
    Users can add words to a collection of “Favourites” by clicking on the heart button. When they open
    their collection of favourite words, they will be automatically sorted by the date the word was
    generated in descending order. They can be deleted one by one – by swiping left, or multiple in one
    go – by long-pressing on a word’s row in the RecyclerView. The user has 10 seconds to undo the
    deletion if they want to.
    Admin users can insert new words through the word of the day control panel in the Vue.js web
    application.
    </p>
</details>


<details>
    <summary>Translator</summary>
    <p markdown="1">
    The translator service helps users learn new phrases and words on the go. The only requirement from
    the user is to enter the text they would like to translate. By default, the translator would use the user’s
    language preferences as source and target languages of the translation. The languages can easily be
    swapped by clicking on the “swap” button. This will make the translator use their source language as
    a target and their target language as a source. The translator uses Google Translation API to perform
    all translations. The parameters needed for the API calls are simply the source + target languages and
    the text to be translated.
    There are multiple approaches to entering text for translations:
    * Simple text input – this is self-explanatory. The user would enter the text in the text box and
    click on the “Translate” button.
    * Speech to text conversion – utilises the built-in speech to text conversion accessibility feature
    of any Android device. It requires the user to record their voice and converts it to plain text.
    * Text recognition from image – uses that device’s camera to capture a snapshot and sends the
    image to Firebase Machine Learning API. The image gets processed and the text result is
    returned to the Android application. The text is then automatically translated without the
    need to click on the “Translate” button.
    After a successful translation is carried out, the result gets displayed in a text box. The user can use
    the result to perform multiple actions with it:
    * Copy the translation to their clipboard – enables quick copy action, handy for users that would
    like to share the translation with somebody else or use it in another application.
    * Play a pronunciation - an audio recording of the translation, pronounced by a computerised
    speaker. This was achieved by using a third-party API from Microsoft Azure – Cognitive
    Services. It requires text input and language options parameters to be send via HTTP request.
    The result it returns is the audio file with pronunciation. This file is cached in the local storage
    and used to play the pronunciation.
    * Add the translation to one or more collections – enables the user to save their translations for
    later use and arrange them in custom collections. By default, every user has a “Favourites”
    collection, however they can create as many new collections as they like. Before creating a
    collection, it is validated against user’s remaining collection names, to ensure the name is
    unique.
    Once the user has created some translations and has added them to some collection, they have the
    option to delete a whole collection, or delete single/multiple translations from a particular collection
    in one go. They can also copy over translations from one collection to another and play pronunciations.
    Pronouncing a translation is only available when the user has selected a single item. If they select
    multiple translations, their options will be delete/copy.
    The translator collections support applying filters to the entries in them. This means if a user wants to
    display only translations with a source language of X and/or target language of Y, they can do so in
    two clicks. By default, the filter applied corresponds to their source and target language preferences.
    Besides filtering, users can search for a particular translation that matches the query they have
    entered. The search is fuzzy, so they enter a phrase that they have translated and find the translation
    of that phrase, or vice versa.
    Admin users can edit user’s translation collections with a JSON viewer if the object is complex, or with
    a form populated with all necessary fields. This can be done only in the Vue.js admin application.
    </p>
</details>


<details>
    <summary>Quiz game</summary>
    <div markdown="1">
    The quiz game’s aim is to help users improve their language learning abilities in a fun and engaging
    way. There are various topics available for the user to choose. Generally speaking, the questions are
    translated to match the user’s language preferences. However, part of the question would ask the
    user to think of what the translation of specific word/phrase is in their target language:
    What is the English translation of the Bulgarian word “куче”?
    The topics and questions are retrieved from the Firebase database. When the user selects a topic, the
    order of the questions gets randomised and presented to them in a sequential manner. Each question
    would have four possible answers. One of the answers is correct. The quiz game implements a score
    tracking system. When a user answers a question correctly, they earn one point. If their answer is
    wrong, they lose one point. However, if their current score is zero, their points cannot go below that.
    Whenever they give an answer to a question, they will receive feedback from the application, whether
    it was correct or not. The user has a time limit to answer questions – 20 seconds. When/If the time
    runs out, they are presented with options to either answer more questions of the same topic or pick
    a new topic to play.
    Users are able to compare their score against other players’. This can be done in the Quiz Game
    Leaderboard. It represents a score board of all usernames and their corresponding score for the
    current month, as well as sum of their score for every month this year (yearly score). The monthly and
    yearly scores can be sorted in ascending and descending order. To make it easier for the user to find
    themselves in the score boards, their row is highlighted in green.
    Admin users can add new topics with questions or individual questions to an existing topic. This can
    be done through the administrative Vue.js web application. The data to be inserted has to be in JSON
    format. To ensure the JSON data that an admin user inputs is valid, I have implemented a JSON editor
    with real-time validation. Whenever it contains an error, the editor would highlight the row and
    provide feedback on mouse hover. The submission of JSON data will only be possible once it is deemed
    valid by the validator.
    </div>
</details>