/*  
App js file used to run the user app - most of this was auto generated and only added very few 
*/
// needed modules 
var createError = require('http-errors');
var express = require('express');
const serverless = require("serverless-http");
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
const mongoose = require("mongoose");
var config = require("./config");
let conversationRouter = require('./routes/conversation');

// way of connecting to the db - this was fun to play with... 
let uri = `mongodb+srv://${config.database.username}:${config.database.password}@${config.database.host}`;
// Connect using mongoose
// some of the code appears to be decapricated with the lts version of Node... 
(async function connectToMongoDB() {
  try {
    await mongoose.connect(uri, {
      useUnifiedTopology: true,
      useNewUrlParser: true,
    });
    console.log("DB successfully connected");
  } catch (e) {
    console.log("DB connection error", e);
  }
})();
var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

// required routers 
app.use('/conversations', conversationRouter);



// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports.handler = serverless(app);