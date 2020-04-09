const crypto = require("crypto");
const multer = require('multer');

const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'public/uploads/')
    },
    filename: function (req, file, cb) {
        let extArray = file.mimetype.split("/");
        let extension = extArray[extArray.length - 1];
        cb(null, crypto.randomBytes(16).toString("hex") + "." + extension);
    }
})
const upload = multer({ storage: storage, dest: 'public/uploads/' })

module.exports = upload;
