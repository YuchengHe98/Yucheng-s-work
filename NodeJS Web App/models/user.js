const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
    nickname: {
        type: String,
        required: true,
        minlength: 3,
        maxlength: 20,
    },
    username: {
        type: String,
        required: true,
        index: true,
        unique: true,
    },
    password: String,
    email: String,
    description: String,
});

UserSchema.methods.setPassword = function (password) {
    this.password = password;
};
UserSchema.methods.validPassword = function(password) {
    return this.password === password;
};

let User = mongoose.model('user', UserSchema);

module.exports = User;
