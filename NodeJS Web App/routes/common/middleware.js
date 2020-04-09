const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const User = require('../../models/user');


passport.serializeUser(function (user, done) {
    done(null, user._id);
});

passport.deserializeUser(function (userId, done) {
    User.findById(userId, (err, user) => done(err, user));
});


const local = new LocalStrategy((username, password, done) => {
    User.findOne({username: username})
        .then(user => {
            if (!user || !user.validPassword(password)) {
                done(null, false, {ok: false, message: 'Invalid username/password'});
            } else {
                done(null, user);
            }
        })
        .catch(e => done(e));
});
passport.use('local', local);


function usePassport(app) {
    app.use(passport.initialize());
    app.use(passport.session());
}

module.exports = {
    usePassport: usePassport,
};
