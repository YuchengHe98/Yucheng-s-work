function checkForErrors(isLoginCorrect) {
    if (isLoginCorrect == 0) {
        alert('login or password is incorrect');
    }
}

function initIntelligentWeb() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker
            .register('./service-worker.js')
            .then(function () {
                console.log('Service Worker Registered');
            })
            .catch (function (error){
                console.log('Service Worker NOT Registered '+ error.message);
            });
    }
    //check for support
    if ('indexedDB' in window) {
        initDatabase();
        // console.log('Running Successful')
    }
    else {
        console.log('This browser doesn\'t support IndexedDB');
    }
}

function onSubmitRegister() {
    var formArray = $('form').serializeArray();
    var data = {};
    for (index in formArray) {
        data[formArray[index].name] = formArray[index].value;
    }

    $.ajax({
        url: '/register',
        data: data,
        dataType: 'json',
        type: 'POST',
        success: function (result) {
            if (result.ok) {
                window.location.href = '/login';
            } else {
                alert('Error: ' + result.message);
            }
        },
        error: function (xhr, status, error) {
            alert('Error: ' + error.message);
        }
    });
    register_user(data);
    event.preventDefault();
}

function onLogin() {
    var formArray = $('form').serializeArray();
    var data = {};
    for (let index in formArray) {
        data[formArray[index].name] = formArray[index].value;
    }

    console.log('DAMN MAN, ', data);
    $.ajax({
        url: '/login',
        data: data,
        type: 'POST',
        success: function (result) {
            console.log(result);
            window.location.href = '/home';
        },
        error: function (xhr, status, error) {
            console.log(xhr);
            if (xhr.status === 401 || xhr.status === 400) {
                alert('Error: ' + 'Invalid username/password');
            } else {
                alert('Error: ' + error.message);
            }
        }
    });
    event.preventDefault();
}

function onModify() {
    var formArray = $('form').serializeArray();
    var data = {};
    for (index in formArray) {
        data[formArray[index].name] = formArray[index].value;
    }
    console.log(data);

    $.ajax({
        url: '/modify',
        data: data,
        dataType: 'json',
        type: 'POST',
        success: function (result) {
            if (result.ok) {
                window.location.href = '/profile';
            } else {
                alert('Error: ' + result.message);
            }
        },
        error: function (xhr, status, error) {
            alert('Error: ' + error.message);
        }
    });
    event.preventDefault();
}

function postData(url, redirect) {
    var formArray = $('form').serializeArray();
    var data = {};
    for (index in formArray) {
        data[formArray[index].name] = formArray[index].value;
    }
    console.log(data);
    $.ajax({
        url: url,
        data: data,
        dataType: 'json',
        type: 'POST',
        success: function (result) {
            if (result.ok) {
                window.location.href = redirect;
            } else {
                alert('Error: ' + result.message);
            }
        },
        error: function (xhr, status, error) {
            alert('Error: ' + error.message);
        }
    });
    event.preventDefault();
}

var onChangePassword = () => postData('/changePassword', '/profile');

function searchAPI(url) {
    var container = document.querySelector('#container');
    container.innerHTML = '';
    $.ajax({
        url: url,
        dataType: 'json',
        type: 'GET',
        success: function (result) {
            if (result.ok) {
                if (result.result.length === 0) {
                    container.insertAdjacentHTML('beforeend',
                        `<h1>No result.</h1>`);
                } else {
                    for (let event of result.result) {
                        container.insertAdjacentHTML('beforeend',
                            `<div class="card" style="width: 100%;">
    <div class="crop">
        <img onerror="this.src='/images/music.jpg';" src="/uploads/${event.imagePath}" class="card-img-top" alt="...">
    </div>
              <div class="card-body">
                  <p class="card-text">
                  ${event.intro}
                  </p>
              </div>
              <ul class="list-group list-group-flush">
                  <li class="list-group-item">${event.name}</li>
                  <li class="list-group-item">Location: ${event.address}</li>
              </ul>
              <div class="card-body">
                  <a href="/event/item/${event._id}" class="card-link">Look up</a>
              </div>
          </div>`);
                    }
                }
            } else {
                alert('Error: ' + result.message);
            }
        },
        error: function (xhr, error, status) {
            alert('Error: ' + status);
        }
    });
}
