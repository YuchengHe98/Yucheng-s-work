const express = require('express');
const router = express.Router();
const {loggedInOnly} = require('./common/session');
const validator = require('./common/validator');
const Event = require('../models/event');
const response = require('./common/response');
const upload = require('./common/upload');

/* Create event. */
router.get('/create', loggedInOnly, function (req, res) {
    res.render('event/create', {title: 'Create event', message: ''});
});
router.post('/create', [loggedInOnly, upload.single('image')], function (req, res) {
    let message = validator.eventIntro(req.body) ||
        validator.eventName(req.body) ||
        validator.location(req.body);
    if (message) {
        return res.render('event/create', {title: 'Create event', message: message});
    } else {
        let event = new Event();
        event.name = req.body.eventName;
        event.intro = req.body.eventIntro;
        event.creator = req.user._id;
        event.imagePath = req.file ? req.file.filename : '';
        event.location = {type: 'Point', coordinates: [req.body.lat, req.body.lng]};
        event.address = req.body.address;
        event.save((err) => {
            if (!err) {
                return res.redirect(`/event/item/${event._id}`);
            } else {
                return res.render('event/create', {title: 'Create event', message: err.message});
            }
        });
    }
});

/* Search event. */
router.get('/search', loggedInOnly, function (req, res) {
    if (req.query.event) {
        Event.find({$text: {$search: req.query.event}}, (err, result) => {
            if (result) {
                return response.json(res, {ok: true, result: result});
            } else {
                return response.json(res, {ok: true, result: []});
            }
        });
    } else if (!validator.latLng(req.query.lat, req.query.lng)) {
        Event.find({
            location: {
                $nearSphere: {
                    $geometry: {
                        type: 'Point',
                        coordinates: [parseFloat(req.query.lat), parseFloat(req.query.lng)]
                    },
                    $maxDistance: 50 * 1000,
                }
            }
        }, (err, result) => {
            console.log(err);
            console.log(result);
            if (result) {
                return response.json(res, {ok: true, result: result});
            } else {
                return response.json(res, {ok: true, result: []});
            }
        });
    } else if (req.query.fromDate && req.query.toDate) {
        let from = new Date(req.query.fromDate);
        let to = new Date(req.query.toDate);
        Event.find({createdAt: {$gt: from, $lt: to}}, (err, result) => {
            if (result) {
                return response.json(res, {ok: true, result: result});
            } else {
                return response.json(res, {ok: true, result: []});
            }
        });
    } else {
        return response.json(res, {ok: true, result: []});
    }
});

/* Get event. */
router.get('/item/:id', loggedInOnly, function (req, res) {
    Event.findById(req.params.id).populate({
        path: 'stories', populate: {path: 'creator', select: 'nickname'}
    }).exec((err, event) => {
        if (!event) {
            return res.send(404);
        } else {
            return res.render('event/event', {title: event.name, event: event});
        }
    });
});

module.exports = router;
