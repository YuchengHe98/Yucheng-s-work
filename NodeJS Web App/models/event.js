const mongoose = require('mongoose');

const EventSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        index: true,
        minlength: 3,
        maxlength: 20,
    },
    intro: {
        type: String,
        required: true,
    },
    location: {
        type: {
            required: true,
            type: String,
            enum: ['Point'],
        },
        coordinates: {
            required: true,
            default: [0, 0],
            type: [Number],
        }
    },
    address: {
        type: String,
        required: true,
    },
    imagePath: String,
    creator: {type: mongoose.Schema.Types.ObjectId, ref: 'user'},
    createdAt: {type: Date, default: Date.now},
    stories: [{type: mongoose.Schema.Types.ObjectId, ref: 'story'}]
});
EventSchema.index({name: 'text'});
EventSchema.index({location: '2dsphere'});
const Event = mongoose.model('event', EventSchema);
Event.ensureIndexes(err => {
    if (err) console.log(err);
});

module.exports = Event;
