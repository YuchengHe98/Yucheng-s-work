const mongoose = require('mongoose');

mongoose.set('useNewUrlParser', true);
mongoose.set('useCreateIndex', true);
mongoose.connect('mongodb://localhost:27017/IntelligentWeb');

let db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:'));
db.once('open', () => {
    console.log('Mongodb connection established');
});
db.once('close', () => {
    console.log('Mongodb connection closed');
});

module.exports = db;
