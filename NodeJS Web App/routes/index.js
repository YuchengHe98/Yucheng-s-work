const express = require('express');
const passport = require('passport');
const User = require('../models/user');
const response = require('./common/response');
const validator = require('./common/validator');
const Event = require('../models/event');
const {loggedInOnly, loggedOutOnly} = require('./common/session');
let router = express.Router();

/* GET index page. */
router.get('/', loggedInOnly, function (req, res, next) {
    res.redirect('/home');
});

router.get('/register', loggedOutOnly, function (req, res, next) {
    res.render('index', {title: 'Register'});
});

/* Register a new user */
router.post('/register', loggedOutOnly, function (req, res, next) {
    let userData = req.body;
    let data = {ok: false, message: 'Please correct input data'};
    if (!userData || !userData.nickname || !userData.username || !userData.password || !userData.email) {
        return response.json(res, data);
    }
    if (userData.password.length > 20 || userData.password.length < 6) {
        data.message = 'Password should be 6-20 characters or symbols.';
        return response.json(res, data);
    }

    let user = new User();
    user.nickname = userData.nickname;
    user.username = userData.username;
    user.email = userData.email ? userData.email : '';
    user.setPassword(userData.password);
    user.save((err, user) => {
        console.log(user);
        if (!err) {
            data = {ok: true};
        } else {
            data = {ok: false, message: err.message};
        }
        response.json(res, data);
    });
});

router.get('/login', loggedOutOnly, function (req, res) {
    res.render('login', {title: 'Double Click', login_is_correct: true});
});

router.post('/login', passport.authenticate('local', {
    successRedirect: '/home',
    failureRedirect: '/login',
    failureFlash: true
}));

router.get('/map', loggedInOnly, function (req, res, next) {
    res.render('map', {title: 'Map'});
});

router.get('/create', loggedInOnly, function (req, res, next) {
    res.redirect('/event/create');
});

router.get('/home', loggedInOnly, function (req, res, next) {
    Event.find({}).limit(20).sort({createdAt: -1}).exec((err, events) => {
        events = events || [];
        res.render('home', {title: 'Home', events: events});
    });
});

router.get('/search', loggedInOnly, function (req, res, next) {
    res.render('search', {title: 'Search'});
});

router.get('/profile', loggedInOnly, function (req, res, next) {
    res.render('profile', {title: 'Profile', user: req.user});
});

router.get('/modify', loggedInOnly, function (req, res, next) {
    res.render('modify', {title: 'Modify', user: req.user});
});

router.post('/modify', loggedInOnly, function (req, res, next) {
    let data = {ok: false, message: ''};

    data.message = validator.nickname(req.body) || validator.description(req.body);
    if (data.message) {
        return response.json(res, data);
    }

    User.update({_id: req.user._id}, {
        $set: {
            nickname: req.body.nickname, description: req.body.description,
        }
    }, (err) => {
        if (err) {
            data.message = error.message;
        } else {
            data.ok = true;
            req.user.nickname = req.body.nickname;
            req.user.description = req.body.description;
        }
        return response.json(res, data);
    });
});

router.get('/changePassword', loggedInOnly, function (req, res) {
    res.render('changePassword', {title: 'Change password', user: req.user});
});

router.post('/changePassword', loggedInOnly, function (req, res) {
    let data = {ok: false, message: ''};

    data.message = validator.password(req.body, 'oldPassword') || validator.password(req.body, 'newPassword');
    if (data.message) {
        return response.json(res, data);
    }

    User.findById(req.user._id, (err, user) => {
        if (user.validPassword(req.body.oldPassword)) {
            user.setPassword(req.body.newPassword);
            user.save((err) => {
                if (err) {
                    data.message = error.message;
                } else {
                    data.ok = true;
                }
                return response.json(res, data);
            });
        } else {
            data.message = 'Your old password is wrong';
            return response.json(res, data);
        }
    });
});

router.get('/logout', loggedInOnly, (req, res) => {
    res.clearCookie('user_sid');
    res.redirect('/');
});

module.exports = router;
