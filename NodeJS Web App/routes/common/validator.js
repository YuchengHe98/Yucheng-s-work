function password(reqBody, fieldName) {
    let value = reqBody[fieldName];
    if (!value) {
        return 'Please input your ' + fieldName;
    }
    if (value.length > 20 || value.length < 6) {
        return 'Password should contain 6-20 characters';
    }
    return '';
}

function nickname(reqBody) {
    if (!reqBody.nickname) {
        return 'Please input your nickname';
    }
    if (reqBody.nickname.length > 20 || reqBody.nickname.length < 3) {
        return 'Nickname should contain 3-20 characters';
    }
    return '';
}

function description(reqBody) {
    if (reqBody.description && reqBody.description.length > 60) {
        return 'Description should contain less than 60 characters';
    }
    return '';
}

function eventName(reqBody) {
    if (!reqBody.eventName) {
        return 'Please input event name';
    }
    if (reqBody.eventName.length > 50 || reqBody.eventName.length < 3) {
        return 'EventName should contain 3-50 characters';
    }
    return '';
}

function eventIntro(reqBody) {
    if (reqBody.eventIntro && reqBody.eventIntro.length > 60) {
        return 'EventIntro should contain less than 60 characters';
    }
    return '';
}

function story(reqBody) {
    if (!reqBody.content) {
        return 'Please input your story content';
    }
    if (reqBody.content && reqBody.content.length > 140) {
        return 'content should contain less than 140 characters';
    }
    return '';
}

function location(reqBody) {
    let address = reqBody.address;
    if (!address) {
        return 'Please input your location';
    }
    return latLng(reqBody.lat, reqBody.lng);
}

function latLng(lat, lng) {
    let _lat = parseFloat(lat);
    let _lng = parseFloat(lng);
    if (!_lat || !_lng) {
        return 'Invalid location';
    }
    if (_lat > 90 || _lat < -90 || _lng > 180 || _lng < -180) {
        return 'Invalid location';
    }
    return '';
}


module.exports = {
    password: password,
    nickname: nickname,
    description: description,
    eventName: eventName,
    eventIntro: eventIntro,
    story: story,
    location: location,
    latLng: latLng,
};
