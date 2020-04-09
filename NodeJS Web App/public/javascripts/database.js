var dbPromise;

/**
 * it inits the database
 */
const ASS_DB_NAME = 'db_intelligentweb_1';

const USER_STORIES = 'store_user_stories';
const USERS = 'store_users';
const EVENTS = 'store_events';

function initDatabase(){
    dbPromise = idb.openDb(ASS_DB_NAME, 1, function(upgradeDb) {
        if (!upgradeDb.objectStoreNames.contains(USER_STORIES)) {
            var userstoriesDB = upgradeDb.createObjectStore(USER_STORIES, {keyPath: 'id', autoIncrement:true});
            userstoriesDB.createIndex('text', 'text', {unique:false});
            userstoriesDB.createIndex('date','date',{unique:false});
            userstoriesDB.createIndex('event_id','event_id',{unique:false})
            userstoriesDB.createIndex('user_id','user_id',{unique:false})
            userstoriesDB.createIndex('picture','picture',{unique:false})
        }
        if (!upgradeDb.objectStoreNames.contains(USERS)) {
            var usersDB = upgradeDb.createObjectStore(USERS, {keyPath: 'id', autoIncrement:true});
            usersDB.createIndex( 'name','name', {unique:false});
            usersDB.createIndex('email','email',{unique:true});
            usersDB.createIndex('password','password',{unique:false})
            usersDB.createIndex('username','username',{unique:true})
        }
        if (!upgradeDb.objectStoreNames.contains(EVENTS)) {
            var eventsDB = upgradeDb.createObjectStore(EVENTS, {keyPath: 'id', autoIncrement:true});
            eventsDB.createIndex('name','name',{unique:false});
            eventsDB.createIndex('description','description',{unique:false});
            eventsDB.createIndex('location','location',{unique:false})
            eventsDB.createIndex('date','date',{unique:false})
        }
        dbPromise.then(async db => { // async is necessary as we use await below
            console.log('running?');
            var tx = db.transaction('store_user_stories', 'readwrite');
            var store = tx.objectStore('store_user_stories');
            var first_user_story = {
                event_id: 1,
                user_id: 1,
                text: 'amazing technique',
                sb: 'cnm',
                date: new Date().getTime()
            };
            await store.add(first_user_story); //await necessary as add return a promise
            return tx.complete;
        }).then(function () {
            console.log('new post to the database! ');
        }).catch(function (error) {
            console.log('biefangqi zai shishi', error);
        });
    });
}

function register_user(userinfo) {
    console.log('kaishihapi!!!');
    dbPromise = idb.openDb(ASS_DB_NAME, 1, function(upgradeDb){
        if (!upgradeDb) { initDatabase(); }
    });
    dbPromise.then(async db => { // async is necessary as we use await below
        console.log('running?123');
        var tx = db.transaction('store_users', 'readwrite');
        var store = tx.objectStore('store_users');
        var create_user = userinfo;
        await store.add(create_user); //await necessary as add return a promise
        return tx.complete;
    }).then(function () {
        console.log('new post to the database!123 ');
    }).catch(function (error) {
        console.log('error', error)
    });

}

function getLoginData(loginObject, cb) {
    dbPromise = idb.openDb(ASS_DB_NAME, 1, function(upgradeDb){
        if (!upgradeDb) { initDatabase(); }
    });
    dbPromise.then(function (db) {
        // console.log('fetching: '+login);
        console.log(loginObject)
        var tx = db.transaction('store_users', 'readonly');
        var store = tx.objectStore('store_users');
        var index = store.index('username');
        return index.get(IDBKeyRange.only(loginObject.username));
    }).then((res) => {
        if (cb) {cb(res)}
        // cb(res);
    }).catch((error) => {
        console.log('WATCH OUT: ', error);
        alert("login or password incorrect");
    });
}
    // then(function (foundObject) {
    //     console.log(foundObject.username)
    //     if (foundObject && (foundObject.username==loginObject.username &&
    //         foundObject.password==loginObject.password)){
    //         console.log("login successful");
    //         alert('login successful');
    //         sendAjaxQuery('/login', {boolean: 1});
    //     } else {
    //         alert("login or password incorrect");
    //         // var sss = JSON.stringify(myObject, replacer);
    //         // var sss = JSON.stringify({boolean:0});
    //     } });
