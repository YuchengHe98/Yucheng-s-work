const express = require('express');
const router = express.Router();
const {loggedInOnly} = require('./common/session');
const validator = require('./common/validator');
const Event = require('../models/event');
const Story = require('../models/story');
const response = require('./common/response');
const upload = require('./common/upload');

/* Create story. */
router.get('/create/:eventId', loggedInOnly, function (req, res) {
    Event.findById(req.params.eventId, (err, event) => {
        if (!event) {
            return res.send(404);
        } else {
            res.render('story/create', {title: 'Create story', message: '', event: event});
        }
    });
});
router.post('/create/:eventId', [loggedInOnly, upload.single('image')], function (req, res) {
    Event.findById(req.params.eventId, (err, event) => {
        if (!event) {
            return res.send(404);
        } else {
            let message = validator.story(req.body);
            if (message) {
                return res.render('story/create', {title: 'Create story', message: message, event: event});
            } else {
                let story = new Story();
                console.log(req.file);
                story.content = req.body.content;
                story.location = req.body.eventName;
                story.creator = req.user._id;
                story.event = event._id;
                story.imagePath = req.file ? req.file.filename : '';
                story.save((err) => {
                    if (!err) {
                        event.stories.push(story);
                        event.save(() => {
                            return res.redirect(`/event/item/${event._id}`);
                        });
                    } else {
                        return res.render('story/create', {title: 'Create story', message: message, event: event});
                    }
                });
            }
        }
    });
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
    } else {
        return response.json(res, {ok: true, result: []});
    }
});

/* Get event. */
router.get('/item/:id', loggedInOnly, function (req, res) {
    Event.findById(req.params.id, (err, event) => {
        if (!event) {
            return res.send(404);
        } else {
            return res.render('event/event', {title: event.name, event: event});
        }
    });
});


module.exports = router;
