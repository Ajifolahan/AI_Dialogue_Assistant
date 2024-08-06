let express = require('express');
let router = express.Router();
let Conversation = require('../models/conversation');

// Create a new conversation
router.post('/', async (req, res) => {  
    try {
        const conversation = new Conversation(req.body);
        await conversation.save();
        res.status(201).send(conversation);
    } catch (error) {
        console.error('Error creating conversation:', error);
        res.status(400).send({ error: error.message });
    }
});

// get all conversations from  the user 
router.get('/:userId', async (req, res) => {  
    try {
        const conversations = await Conversation.find({ userId: req.params.userId });
        res.send(conversations);
    } catch (error) {
        res.status(500).send(error);
    }
});

// Get a specific conversation by ID
router.get('/:userId/:conversationId', async (req, res) => {  
    try {
        const conversation = await Conversation.findOne({ userId: req.params.userId, conversationId: req.params.conversationId });
        if (!conversation) {
            return res.status(404).send();
        }
        res.send(conversation);
    } catch (error) {
        res.status(500).send(error);
    }
});

// Add a message to a conversation
router.post('/:userId/:conversationId/messages', async (req, res) => {  
    try {
        const conversation = await Conversation.findOne({ userId: req.params.userId, conversationId: req.params.conversationId });
        if (!conversation) {
            return res.status(404).send();
        }
        conversation.messages.push(req.body);
        await conversation.save();
        res.send(conversation);
    } catch (error) {
        res.status(400).send(error);
    }
});

router.get('/check-conversation/:userId/:conversationId', async (req, res) => {
    const { userId, conversationId } = req.params;

    try {
        const conversation = await Conversation.findOne({ userId, conversationId });

        if (!conversation || conversation.messages.length === 0) {
            return res.json({ suppressInitialMessage: false });
        }

        const lastMessage = conversation.messages[conversation.messages.length - 1];
        const suppressInitialMessage = lastMessage.sender === 'ai';

        res.json({ suppressInitialMessage });
    } catch (error) {
        console.error('Error checking conversation:', error);
        res.status(500).json({ error: 'Internal Server Error' });
    }
});


//delete convo
router.delete('/:userId/:conversationId', async (req, res) => {
    try {
        const conversation = await Conversation.findOneAndDelete({ userId: req.params.userId, conversationId: req.params.conversationId });
        if (!conversation) {
            return res.status(404).send();
        }
        res.send(conversation);
    } catch (error) {
        res.status(500).send(error);
    }
})

module.exports = router;
