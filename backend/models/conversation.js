const mongoose = require('mongoose');

const messageSchema = new mongoose.Schema({
    sender: {
        type: String,
        enum: ['user', 'ai'],
        required: true
    },
    message: {
        type: String,
        required: function () {
             return !this.imagePath; // message is required only if imagePath is not provided
        }
//        required: true
    },
    timestamp: {
        type: Date,
        default: Date.now,
        required: true
    },
    imagePath: {
        type: String
    }
});


const conversationSchema = new mongoose.Schema({
    userId: {
        type: String,
        required: true
    },
    topic: {
        type: String,
        required: true
    },
    language: {
        type: String,
        required: true
    },
    conversationId: {
        type: String,
        required: true
    },
    messages: [messageSchema],
    createdAt: {
        type: String,
        required: true
    },
    updatedAt: {
        type: String,
        required: true
    }
});

// Update the updatedAt field before each save
conversationSchema.pre('save', function(next) {
    this.updatedAt = Date.now();
    next();
});

const Conversation = mongoose.model('Conversation', conversationSchema);

module.exports = Conversation;
