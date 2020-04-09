const mongoose = require('mongoose');

const StorySchema = new mongoose.Schema({
    content: {
        type: String,
        required: true,
    },
    location: {
        type: {
            type: String,
            enum: ['Point'],
        },
        coordinates: {
            type: [Number],
        }
    },
    imagePath: String,
    creator: { type: mongoose.Schema.Types.ObjectId, ref: 'user' },
    event: { type: mongoose.Schema.Types.ObjectId, ref: 'event' },
    createdAt: { type: Date, default: Date.now },
});
StorySchema.index({name: 'text'});
const Story = mongoose.model('story', StorySchema);

module.exports = Story;
