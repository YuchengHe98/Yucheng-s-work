const loggedInOnly = (req, res, next) => {
    if (req.isAuthenticated()) next();
    else res.redirect('/login');
};

const loggedOutOnly = (req, res, next) => {
    if (req.isUnauthenticated()) next();
    else res.redirect('/home');
};

module.exports = {
    loggedInOnly: loggedInOnly,
    loggedOutOnly: loggedOutOnly,
};
