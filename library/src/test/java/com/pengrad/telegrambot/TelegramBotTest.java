package com.pengrad.telegrambot;

import com.pengrad.telegrambot.impl.TelegramBotClient;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.passport.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.pengrad.telegrambot.request.ContentTypes.VIDEO_MIME_TYPE;
import static org.junit.Assert.*;

/**
 * stas
 * 5/2/16.
 */
public class TelegramBotTest {

    TelegramBot bot;
    Integer chatId;
    Long groupId;
    Integer forwardMessageId = 1;
    String stickerId = "BQADAgAD4AAD9HsZAAGVRXVaYXiJVAI";
    String channelName = "@bottest";
    Long channelId = -1001002720332L;
    Integer memberBot = 215003245;
    String privateKey;
    String testPassportData;

    Path resourcePath = Paths.get("src/test/resources");
    File imageFile = resourcePath.resolve("image.jpg").toFile();
    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
    File stickerFile = resourcePath.resolve("imageSticker.png").toFile();
    File audioFile = resourcePath.resolve("beep.mp3").toFile();
    byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
    File docFile = resourcePath.resolve("doc.txt").toFile();
    byte[] docBytes = Files.readAllBytes(docFile.toPath());
    File videoFile = resourcePath.resolve("tabs.mp4").toFile();
    byte[] videoBytes = Files.readAllBytes(videoFile.toPath());
    File videoNoteFile = resourcePath.resolve("video_note.mp4").toFile();
    byte[] videoNoteBytes = Files.readAllBytes(videoNoteFile.toPath());
    String certificateFile = resourcePath.resolve("cert.pem").toString();
    String someUrl = "http://google.com/";
    String audioFileId = "CQADAgADXAADgNqgSevw7NljQE4lAg";
    String docFileId = "BQADAgADuwADgNqYSaVAUsHMq6hqAg";
    String voiceFileId = "AwADAgADYwADuYNZSZww_hkrzCIpAg";
    String videoFileId = "BAADAgADZAADuYNZSXhLnzJTZ2yvAg";
    String photoFileId = "AgADAgADDKgxG7mDWUlvyFIJ9XfF9yszSw0ABBhVadWwbAK1z-wIAAEC";
    String gifFileId = "CgADAgADfQADgNqgSTt9SzatJhc3Ag";
    String withSpaceFileId = "BAADAgADZwADkg-4SQI5WM0SPNHrAg";
    String stickerSet = "testset_by_pengrad_test_bot";
    String imageUrl = "https://telegram.org/img/t_logo.png";
    File thumbFile = resourcePath.resolve("thumb.jpg").toFile();
    byte[] thumbBytes = Files.readAllBytes(thumbFile.toPath());
    Integer thumbSize = 3718;
    File gifFile = resourcePath.resolve("anim3.gif").toFile();
    byte[] gifBytes = Files.readAllBytes(gifFile.toPath());

    public TelegramBotTest() throws IOException {
        String token, chat, group;

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("local.properties"));

            token = properties.getProperty("TEST_TOKEN");
            chat = properties.getProperty("CHAT_ID");
            group = properties.getProperty("GROUP_ID");
            privateKey = properties.getProperty("PRIVATE_KEY");
            testPassportData = properties.getProperty("TEST_PASSPORT_DATA");

        } catch (Exception e) {
            token = System.getenv("TEST_TOKEN");
            chat = System.getenv("CHAT_ID");
            group = System.getenv("GROUP_ID");
            privateKey = System.getenv("PRIVATE_KEY");
            testPassportData = System.getenv("TEST_PASSPORT_DATA");
        }

        bot = new TelegramBot.Builder(token).debug().build();
        chatId = Integer.parseInt(chat);
        groupId = Long.parseLong(group);
    }

    @Test
    public void getMe() {
        GetMeResponse response = bot.execute(new GetMe());
        UserTest.checkUser(response.user());
        assertTrue(response.user().isBot());
    }

    @Test
    public void getUpdates() {
        GetUpdates getUpdates = new GetUpdates()
                .offset(870644071)
                .allowedUpdates("")
                .timeout(0)
                .limit(100);
        assertEquals(100, getUpdates.getLimit());
        GetUpdatesResponse response = bot.execute(getUpdates);
        UpdateTest.check(response.updates());
    }

    @Test
    public void getFile() throws IOException {
        GetFileResponse response = bot.execute(new GetFile(withSpaceFileId));
        FileTest.check(response.file());
        String path = bot.getFullFilePath(response.file());

        Request request = new Request.Builder().head().url(path).build();
        Response pathResponse = new OkHttpClient().newCall(request).execute();
        assertTrue(pathResponse.isSuccessful());
    }

    @Test
    public void kickChatMember() {
        BaseResponse response = bot.execute(new KickChatMember(channelName, chatId).untilDate(123));
        assertFalse(response.isOk());
        assertEquals(400, response.errorCode());
        assertEquals("Bad Request: user is an administrator of the chat", response.description());
    }

    @Test
    public void unbanChatMember() {
        BaseResponse response = bot.execute(new UnbanChatMember(channelName, chatId));
        assertFalse(response.isOk());
        assertEquals(400, response.errorCode());
        assertEquals("Bad Request: user is an administrator of the chat", response.description());
    }

    @Test
    public void restrictChatMember() {
        BaseResponse response = bot.execute(
                new RestrictChatMember(groupId, memberBot)
                        .untilDate(100)
                        .canSendMessages(false)
                        .canSendMediaMessages(false)
                        .canSendOtherMessages(false)
                        .canAddWebPagePreviews(false));
        assertTrue(response.isOk());
    }

    @Test
    public void promoteChatMember() {
        BaseResponse response = bot.execute(
                new PromoteChatMember(groupId, memberBot)
                        .canChangeInfo(false)
                        .canPostMessages(false)
                        .canEditMessages(false)
                        .canDeleteMessages(false)
                        .canInviteUsers(false)
                        .canRestrictMembers(false)
                        .canPinMessages(false)
                        .canPromoteMembers(false));
        assertTrue(response.isOk());
    }

    @Test
    public void editMessageText() {
        String text = "Update " + System.currentTimeMillis();

        BaseResponse response = bot.execute(new EditMessageText(chatId, 925, text)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true)
                .replyMarkup(new InlineKeyboardMarkup()));
        assertTrue(response.isOk());
        assertNotNull(((SendResponse) response).message().editDate());

        response = bot.execute(new EditMessageText(channelName, 306, text));
        assertTrue(response.isOk());

        response = bot.execute(new EditMessageText("AgAAAN3wAQCj_Q4DjX4ok5VEUZU", text));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: MESSAGE_ID_INVALID", response.description());
        }
    }

    @Test
    public void editMessageCaption() {
        String text = "Update " + System.currentTimeMillis() + " <b>bold</b>";

        SendResponse sendResponse = (SendResponse) bot.execute(new EditMessageCaption(chatId, 8124)
                .caption(text)
                .parseMode(ParseMode.HTML)
                .replyMarkup(new InlineKeyboardMarkup()));
        assertTrue(sendResponse.isOk());

        Message message = sendResponse.message();
        assertEquals(text.replace("<b>", "").replace("</b>", ""), message.caption());

        MessageEntity captionEntity = message.captionEntities()[0];
        assertEquals(MessageEntity.Type.bold, captionEntity.type());
        assertEquals((Integer) 21, captionEntity.offset());
        assertEquals((Integer) 4, captionEntity.length());

        BaseResponse response = bot.execute(new EditMessageCaption(channelName, 511).caption(text));
        assertTrue(response.isOk());

        response = bot.execute(new EditMessageCaption("AgAAAPrwAQCj_Q4D2s-51_8jsuU").caption(text));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: MESSAGE_ID_INVALID", response.description());
        }
    }

    @Test
    public void editMessageReplyMarkup() {
        String text = "Update" + System.currentTimeMillis();

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                new InlineKeyboardButton(text).url("https://google.com")});

        InlineKeyboardMarkup gameKeyboard = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                new InlineKeyboardButton(text).callbackGame("test game")});

        BaseResponse response = bot.execute(new EditMessageReplyMarkup(chatId, 8124).replyMarkup(keyboard));
        assertTrue(response.isOk());

        response = bot.execute(new EditMessageReplyMarkup(channelName, 511).replyMarkup(keyboard));
        assertTrue(response.isOk());

        response = bot.execute(new EditMessageReplyMarkup("AgAAAPrwAQCj_Q4D2s-51_8jsuU").replyMarkup(gameKeyboard));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: MESSAGE_ID_INVALID", response.description());
        }
    }

    @Test
    public void answerInline() {
        InlineQuery lastInlineQuery = getLastInlineQuery();
        String inlineQueryId = lastInlineQuery != null ? lastInlineQuery.id() : "invalid_query_id";

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                new InlineKeyboardButton("inline game").callbackGame("pengrad test game description"),
                new InlineKeyboardButton("inline ok").callbackData("callback ok"),
                new InlineKeyboardButton("cancel").callbackData("callback cancel"),
                new InlineKeyboardButton("url").url(someUrl),
                new InlineKeyboardButton("switch inline").switchInlineQuery("query"),
                new InlineKeyboardButton("switch inline current").switchInlineQueryCurrentChat("query"),
        });

        InlineQueryResult[] results = new InlineQueryResult[]{
                new InlineQueryResultArticle("1", "title",
                        new InputTextMessageContent("message").disableWebPagePreview(false).parseMode(ParseMode.HTML))
                        .url(someUrl).hideUrl(true).description("desc").thumbUrl(someUrl).thumbHeight(100).thumbWidth(100),
                new InlineQueryResultArticle("2", "title",
                        new InputContactMessageContent("123123123", "na,e").lastName("lastName").vcard("qr vcard")),
                new InlineQueryResultArticle("3", "title", new InputLocationMessageContent(50f, 50f).livePeriod(60)),
                new InlineQueryResultArticle("4", "title",
                        new InputVenueMessageContent(50f, 50f, "title", "address").foursquareId("sqrId").foursquareType("frType")),
                new InlineQueryResultArticle("5", "title", "message"),
                new InlineQueryResultAudio("6", someUrl, "title").caption("cap <b>bold</b>").parseMode(ParseMode.HTML).performer("perf").audioDuration(100),
                new InlineQueryResultContact("7", "123123123", "name").lastName("lastName").vcard("tt vcard")
                        .thumbUrl(someUrl).thumbHeight(100).thumbWidth(100),
                new InlineQueryResultDocument("8", someUrl, "title", "application/pdf").caption("cap <b>bold</b>").parseMode(ParseMode.HTML).description("desc")
                        .thumbUrl(someUrl).thumbHeight(100).thumbWidth(100),
                new InlineQueryResultGame("9", "pengrad_test_game").replyMarkup(keyboardMarkup),
                new InlineQueryResultGif("10", someUrl, someUrl).caption("cap <b>bold</b>").parseMode(ParseMode.HTML).title("title")
                        .gifHeight(100).gifWidth(100).gifDuration(100),
                new InlineQueryResultLocation("11", 50f, 50f, "title").livePeriod(60)
                        .thumbUrl(someUrl).thumbHeight(100).thumbWidth(100),
                new InlineQueryResultMpeg4Gif("12", someUrl, someUrl).caption("cap <b>bold</b>").parseMode(ParseMode.HTML).title("title")
                        .mpeg4Height(100).mpeg4Width(100).mpeg4Duration(100),
                new InlineQueryResultPhoto("13", someUrl, someUrl).photoWidth(100).photoHeight(100).title("title")
                        .description("desc").caption("cap <b>bold</b>").parseMode(ParseMode.HTML),
                new InlineQueryResultVenue("14", 54f, 55f, "title", "address").foursquareId("frsqrId").foursquareType("frType")
                        .thumbUrl(someUrl).thumbHeight(100).thumbWidth(100),
                new InlineQueryResultVideo("15", someUrl, VIDEO_MIME_TYPE, "text", someUrl, "title").caption("cap <b>bold</b>").parseMode(ParseMode.HTML)
                        .videoWidth(100).videoHeight(100).videoDuration(100).description("desc"),
                new InlineQueryResultVoice("16", someUrl, "title").caption("cap <b>bold</b>").parseMode(ParseMode.HTML).voiceDuration(100),
                new InlineQueryResultCachedAudio("17", audioFileId).caption("cap <b>bold</b>").parseMode(ParseMode.HTML),
                new InlineQueryResultCachedDocument("18", stickerId, "title").caption("cap <b>bold</b>").parseMode(ParseMode.HTML).description("desc"),
                new InlineQueryResultCachedGif("19", gifFileId).caption("cap <b>bold</b>").parseMode(ParseMode.HTML).title("title"),
                new InlineQueryResultCachedMpeg4Gif("21", gifFileId).caption("cap <b>bold</b>").parseMode(ParseMode.HTML).title("title"),
                new InlineQueryResultCachedPhoto("22", photoFileId).caption("cap <b>bold</b>").parseMode(ParseMode.HTML).description("desc").title("title"),
                new InlineQueryResultCachedSticker("23", stickerId),
                new InlineQueryResultCachedVideo("24", videoFileId, "title").caption("cap <b>bold</b>").parseMode(ParseMode.HTML).description("desc"),
                new InlineQueryResultCachedVoice("25", voiceFileId, "title").caption("cap <b>bold</b>").parseMode(ParseMode.HTML),
        };

        BaseResponse response = bot.execute(new AnswerInlineQuery(inlineQueryId, results)
                .cacheTime(100)
                .isPersonal(true)
                .nextOffset("offset")
                .switchPmText("go pm")
                .switchPmParameter("my_pm_parameter")
        );

        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: query is too old and response timeout expired or query ID is invalid", response.description());
        }
    }

    private InlineQuery getLastInlineQuery() {
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        Collections.reverse(updates);
        for (Update update : updates) {
            if (update.inlineQuery() != null) {
                return update.inlineQuery();
            }
        }
        return null;
    }

    @Test
    public void answerCallback() {
        CallbackQuery callbackQuery = getLastCallbackQuery();
        String callbackQueryId = callbackQuery != null ? callbackQuery.id() : "invalid_query_id";

        BaseResponse response = bot.execute(new AnswerCallbackQuery(callbackQueryId)
                .text("answer callback")
                .url("telegram.me/pengrad_test_bot?game=pengrad_test_game")
                .showAlert(false)
                .cacheTime(1));

        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: query is too old and response timeout expired or query ID is invalid", response.description());
        }
    }

    private CallbackQuery getLastCallbackQuery() {
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        Collections.reverse(updates);
        for (Update update : updates) {
            if (update.callbackQuery() != null) {
                return update.callbackQuery();
            }
        }
        return null;
    }

    @Test
    public void getChat() throws MalformedURLException, URISyntaxException {
        Chat chat = bot.execute(new GetChat(groupId)).chat();
        ChatTest.checkChat(chat);
        assertEquals(Chat.Type.supergroup, chat.type());
        assertTrue(chat.title().contains("Test Bot Group"));
        assertTrue(chat.description().contains("New desc"));
        assertNotNull(new URL(chat.inviteLink()).toURI());
        if (chat.pinnedMessage() != null) MessageTest.checkMessage(chat.pinnedMessage());
        assertNull(chat.allMembersAreAdministrators());
        assertNull(chat.stickerSetName());
        assertNull(chat.canSetStickerSet());

        chat = bot.execute(new GetChat(chatId)).chat();
        assertNotNull(chat.firstName());
        assertNotNull(chat.lastName());
    }

    @Test
    public void leaveChat() {
        BaseResponse response = bot.execute(new LeaveChat(chatId));
        assertFalse(response.isOk());
        assertEquals(400, response.errorCode());
        assertEquals("Bad Request: chat member status can't be changed in private chats", response.description());
    }

    @Test
    public void getChatAdministrators() {
        GetChatAdministratorsResponse response = bot.execute(new GetChatAdministrators(groupId));
        for (ChatMember chatMember : response.administrators()) {
            ChatMemberTest.check(chatMember);
            if (chatMember.user().firstName().equals("Test Bot")) {
                assertFalse(chatMember.canBeEdited());
                assertTrue(chatMember.canChangeInfo());
                assertTrue(chatMember.canDeleteMessages());
                assertTrue(chatMember.canInviteUsers());
                assertTrue(chatMember.canRestrictMembers());
                assertTrue(chatMember.canPinMessages());
                assertTrue(chatMember.canPromoteMembers());
            }
        }
    }

    @Test
    public void getChatMember() {
        restrictChatMember();
        ChatMember chatMember = bot.execute(new GetChatMember(groupId, memberBot)).chatMember();
        ChatMemberTest.check(chatMember);
        assertEquals(ChatMember.Status.restricted, chatMember.status());
        assertEquals(Integer.valueOf(0), chatMember.untilDate());
        assertNull(chatMember.canPostMessages());
        assertNull(chatMember.canEditMessages());
        assertTrue(chatMember.isMember());
        assertFalse(chatMember.canSendMessages());
        assertFalse(chatMember.canSendMediaMessages());
        assertFalse(chatMember.canSendOtherMessages());
        assertFalse(chatMember.canAddWebPagePreviews());
    }

    @Test
    public void getChatMembersCount() {
        GetChatMembersCountResponse response = bot.execute(new GetChatMembersCount(chatId));
        assertEquals(2, response.count());
    }

    @Test
    public void getUserProfilePhotos() {
        int offset = 1;
        GetUserProfilePhotosResponse response = bot.execute(new GetUserProfilePhotos(chatId).limit(100).offset(offset));
        UserProfilePhotos photos = response.photos();
        assertEquals(photos.totalCount() - offset, photos.photos().length);
        for (PhotoSize[] photo : photos.photos()) {
            PhotoSizeTest.checkPhotos(photo);
        }

        if (photos.totalCount() > 1) {
            photos = bot.execute(new GetUserProfilePhotos(chatId).limit(1)).photos();
            assertEquals(1, photos.photos().length);
        }
    }

    @Test
    public void sendMessage() {
        SendResponse sendResponse = bot.execute(new SendMessage(chatId, "reply this message").replyMarkup(new ForceReply()));
        MessageTest.checkTextMessage(sendResponse.message());
        assertNotNull(sendResponse.message().from());

        sendResponse = bot.execute(new SendMessage(chatId, "remove keyboard")
                .replyMarkup(new ReplyKeyboardRemove())
                .disableNotification(true)
                .replyToMessageId(8087)
        );
        MessageTest.checkTextMessage(sendResponse.message());
        assertNotNull(sendResponse.message().replyToMessage());

        sendResponse = bot.execute(new SendMessage(chatId, "message with keyboard")
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(false)
                .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton[]{
                        new KeyboardButton("contact").requestContact(true),
                        new KeyboardButton("location").requestLocation(true)})
                        .oneTimeKeyboard(true)
                        .resizeKeyboard(true)
                        .selective(true)));
        MessageTest.checkTextMessage(sendResponse.message());

        sendResponse = bot.execute(new SendMessage(chatId, "simple buttons")
                .replyMarkup(new ReplyKeyboardMarkup(new String[]{"ok", "cancel"})));
        MessageTest.checkTextMessage(sendResponse.message());
    }

    @Test
    public void sendMessageToChannel() {
        String url = "https://google.com/";
        SendMessage request = new SendMessage(channelName, "channel message [GG](" + url + ")").parseMode(ParseMode.Markdown);
        SendResponse sendResponse = bot.execute(request);
        Message message = sendResponse.message();
        MessageTest.checkTextMessage(message);
        assertEquals(url, message.entities()[0].url());
    }

    @Test
    public void sendMessageToChannelId() {
        SendMessage request = new SendMessage(channelId, "channel by id message");
        SendResponse sendResponse = bot.execute(request);
        Message message = sendResponse.message();
        MessageTest.checkTextMessage(message);
    }

    @Test
    public void forwardMessage() {
        SendResponse response = bot.execute(new ForwardMessage(chatId, chatId, forwardMessageId).disableNotification(true));
        Message message = response.message();
        MessageTest.checkMessage(message);
        assertNotNull(message.forwardDate());
        assertNotNull(message.forwardSenderName());
        assertNull(message.forwardFrom());

        message = bot.execute(new ForwardMessage(channelName, channelName, 651)).message();
        assertNotNull(message.authorSignature());
        assertNotNull(message.forwardSignature());
        assertEquals(Integer.valueOf(651), message.forwardFromMessageId());
        Chat chat = message.forwardFromChat();
        assertEquals(channelName, "@" + chat.username());
        assertEquals(Chat.Type.channel, chat.type());
        assertNull(message.forwardSenderName());

        message = bot.execute(new ForwardMessage(chatId, groupId, 352)).message();
        assertEquals(MessageEntity.Type.text_mention, message.entities()[0].type());
        assertNotNull(message.entities()[0].user());
        assertNotNull(message.forwardSenderName());
    }

    @Test
    public void sendAudio() {
        Message message = bot.execute(new SendAudio(chatId, audioFileId)).message();
        MessageTest.checkMessage(message);
        AudioTest.checkAudio(message.audio(), false);

        message = bot.execute(new SendAudio(chatId, audioFile).thumb(thumbFile)).message();
        MessageTest.checkMessage(message);
        AudioTest.checkAudio(message.audio());
        assertEquals(thumbSize, message.audio().thumb().fileSize());

        String cap = "http://ya.ru  <b>bold</b> #audio @pengrad_test_bot", title = "title", performer = "performer";
        ParseMode parseMode = ParseMode.HTML;
        int duration = 100;
        SendAudio sendAudio = new SendAudio(chatId, audioBytes).thumb(thumbBytes).duration(duration)
                .caption(cap).parseMode(parseMode).performer(performer).title(title);
        message = bot.execute(sendAudio).message();
        MessageTest.checkMessage(message);

        Audio audio = message.audio();
        AudioTest.checkAudio(audio);
        assertEquals(cap.replace("<b>", "").replace("</b>", ""), message.caption());
        assertEquals((Integer) 100, audio.duration());
        assertEquals(performer, audio.performer());
        assertEquals(title, audio.title());
        assertEquals(thumbSize, audio.thumb().fileSize());

        MessageEntity captionEntity = message.captionEntities()[0];
        assertEquals(MessageEntity.Type.url, captionEntity.type());
        assertEquals((Integer) 0, captionEntity.offset());
        assertEquals((Integer) 12, captionEntity.length());

        captionEntity = message.captionEntities()[1];
        assertEquals(MessageEntity.Type.bold, captionEntity.type());
        assertEquals((Integer) 14, captionEntity.offset());
        assertEquals((Integer) 4, captionEntity.length());

        assertEquals(MessageEntity.Type.hashtag, message.captionEntities()[2].type());
    }

    @Test
    public void sendDocument() {
        Message message = bot.execute(new SendDocument(chatId, docFileId)).message();
        MessageTest.checkMessage(message);
        DocumentTest.check(message.document());

        message = bot.execute(new SendDocument(chatId, docBytes).thumb(thumbBytes)).message();
        MessageTest.checkMessage(message);
        DocumentTest.check(message.document());
        assertEquals(thumbSize, message.document().thumb().fileSize());

        String caption = "caption <b>bold</b>", fileName = "my doc.zip";
        ParseMode parseMode = ParseMode.HTML;
        message = bot.execute(
                new SendDocument(chatId, docFile).fileName(fileName).thumb(thumbFile).caption(caption).parseMode(parseMode))
                .message();
        MessageTest.checkMessage(message);
        DocumentTest.check(message.document());
        assertEquals(caption.replace("<b>", "").replace("</b>", ""), message.caption());
        assertEquals(fileName, message.document().fileName());
        assertEquals(thumbSize, message.document().thumb().fileSize());

        MessageEntity captionEntity = message.captionEntities()[0];
        assertEquals(MessageEntity.Type.bold, captionEntity.type());
        assertEquals((Integer) 8, captionEntity.offset());
        assertEquals((Integer) 4, captionEntity.length());
    }

    @Test
    public void sendPhoto() throws IOException {
        Message message = bot.execute(new SendPhoto(chatId, photoFileId)).message();
        MessageTest.checkMessage(message);
        PhotoSizeTest.checkPhotos(false, message.photo());

        message = bot.execute(new SendPhoto(chatId, imageFile)).message();
        MessageTest.checkMessage(message);
        PhotoSizeTest.checkPhotos(message.photo());

        String caption = "caption <b>bold</b>";
        message = bot.execute(new SendPhoto(channelName, imageBytes).caption(caption).parseMode(ParseMode.HTML)).message();
        MessageTest.checkMessage(message);
        assertEquals(caption.replace("<b>", "").replace("</b>", ""), message.caption());
        PhotoSizeTest.checkPhotos(message.photo());

        MessageEntity captionEntity = message.captionEntities()[0];
        assertEquals(MessageEntity.Type.bold, captionEntity.type());
        assertEquals((Integer) 8, captionEntity.offset());
        assertEquals((Integer) 4, captionEntity.length());
    }

    @Test
    public void sendSticker() throws IOException {
        Message message = bot.execute(new SendSticker(chatId, stickerId)).message();
        MessageTest.checkMessage(message);
        StickerTest.check(message.sticker(), true, false);

        message = bot.execute(new SendSticker(chatId, imageFile)).message();
        MessageTest.checkMessage(message);
        StickerTest.check(message.sticker(), false, true);

        message = bot.execute(new SendSticker(chatId, imageBytes)).message();
        MessageTest.checkMessage(message);
        StickerTest.check(message.sticker(), false, true);
    }

    @Test
    public void sendVideo() {
        Message message = bot.execute(new SendVideo(chatId, videoFileId)).message();
        MessageTest.checkMessage(message);
        VideoTest.check(message.video(), false);

        message = bot.execute(new SendVideo(chatId, videoFile).thumb(thumbFile)).message();
        MessageTest.checkMessage(message);
        VideoTest.check(message.video());
        assertNotEquals("telegram should generate thumb", thumbSize, message.video().thumb().fileSize());

        String caption = "caption <b>bold</b>";
        Integer duration = 100;
        message = bot.execute(
                new SendVideo(chatId, videoBytes).thumb(thumbBytes)
                        .caption(caption).parseMode(ParseMode.HTML)
                        .duration(duration).height(1).width(2).supportsStreaming(true))
                .message();
        MessageTest.checkMessage(message);
        assertEquals(caption.replace("<b>", "").replace("</b>", ""), message.caption());

        Video video = message.video();
        VideoTest.check(message.video());
        assertEquals(duration, video.duration());
        assertEquals((Integer) 120, video.height());
        assertEquals((Integer) 400, video.width());
        assertNotEquals("telegram should generate thumb", thumbSize, video.thumb().fileSize());

        MessageEntity captionEntity = message.captionEntities()[0];
        assertEquals(MessageEntity.Type.bold, captionEntity.type());
        assertEquals((Integer) 8, captionEntity.offset());
        assertEquals((Integer) 4, captionEntity.length());
    }

    @Test
    public void sendVoice() {
        Message message = bot.execute(new SendVoice(chatId, voiceFileId)).message();
        MessageTest.checkMessage(message);
        VoiceTest.check(message.voice(), false);

        message = bot.execute(new SendVoice(chatId, audioFile)).message();
        MessageTest.checkMessage(message);
        VoiceTest.check(message.voice());

        String caption = "caption <b>bold</b>";
        Integer duration = 100;
        message = bot.execute(new SendVoice(chatId, audioBytes).caption(caption).parseMode(ParseMode.HTML).duration(duration)).message();
        MessageTest.checkMessage(message);
        assertEquals(caption.replace("<b>", "").replace("</b>", ""), message.caption());
        VoiceTest.check(message.voice());
        assertEquals(duration, message.voice().duration());

        MessageEntity captionEntity = message.captionEntities()[0];
        assertEquals(MessageEntity.Type.bold, captionEntity.type());
        assertEquals((Integer) 8, captionEntity.offset());
        assertEquals((Integer) 4, captionEntity.length());
    }

    @Test
    public void getWebhookInfo() {
        GetWebhookInfoResponse response = bot.execute(new GetWebhookInfo());
        WebhookInfoTest.check(response.webhookInfo());
    }

    @Test
    public void setWebhook() throws IOException, InterruptedException {
        String url = "https://google.com";
        Integer maxConnections = 100;
        String[] allowedUpdates = {"message", "callback_query"};
        BaseResponse response = bot.execute(new SetWebhook().url(url).certificate(new File(certificateFile))
                .maxConnections(100).allowedUpdates(allowedUpdates));
        assertTrue(response.isOk());

        Thread.sleep(1000);

        WebhookInfo webhookInfo = bot.execute(new GetWebhookInfo()).webhookInfo();
        assertEquals(url, webhookInfo.url());
        assertTrue(webhookInfo.hasCustomCertificate());
        assertEquals(maxConnections, webhookInfo.maxConnections());
        assertArrayEquals(allowedUpdates, webhookInfo.allowedUpdates());
        assertNotNull(webhookInfo.lastErrorDate());
        assertNotNull(webhookInfo.lastErrorMessage());

        response = bot.execute(new SetWebhook().url("https://google.com")
                .certificate(Files.readAllBytes(new File(certificateFile).toPath())).allowedUpdates(""));
        assertTrue(response.isOk());

        Thread.sleep(1000);

        response = bot.execute(new SetWebhook());
        assertTrue(response.isOk());
    }

    @Test
    public void deleteWebhook() {
        BaseResponse response = bot.execute(new DeleteWebhook());
        assertTrue(response.isOk());
    }

    @Test
    public void sendGame() {
        InlineKeyboardButton[] buttons = {
                new InlineKeyboardButton("inline game").callbackGame("pengrad test game description"),
                new InlineKeyboardButton("inline ok").callbackData("callback ok"),
                new InlineKeyboardButton("cancel").callbackData("callback cancel"),
                new InlineKeyboardButton("url").url(someUrl),
                new InlineKeyboardButton("switch inline").switchInlineQuery("query"),
                new InlineKeyboardButton("switch inline current").switchInlineQueryCurrentChat("query"),
        };

        InlineKeyboardButton[][] inlineKeyboard = new InlineKeyboardButton[1][];
        inlineKeyboard[0] = buttons;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(inlineKeyboard);

        String desc = "pengrad_test_game";
        Message message = bot.execute(new SendGame(chatId, desc).replyMarkup(keyboardMarkup)).message();
        MessageTest.checkMessage(message);
        Game game = message.game();
        GameTest.check(game);
        assertEquals(desc, game.description());

        InlineKeyboardButton[] actualButtons = message.replyMarkup().inlineKeyboard()[0];
        assertEquals(buttons.length, actualButtons.length);
        assertNotNull(actualButtons[0].callbackGame());
        for (int i = 1; i < buttons.length; i++) {
            assertEquals(buttons[i].text(), actualButtons[i].text());
        }
        assertEquals(buttons[1].callbackData(), actualButtons[1].callbackData());
        assertEquals(buttons[2].callbackData(), actualButtons[2].callbackData());
        assertEquals(buttons[3].url(), actualButtons[3].url());
        assertEquals(buttons[4].switchInlineQuery(), actualButtons[4].switchInlineQuery());
        assertEquals(buttons[5].switchInlineQueryCurrentChat(), actualButtons[5].switchInlineQueryCurrentChat());
    }

    @Test
    public void setGameScore() {
        int res = (int) (System.currentTimeMillis() / 1000);
        BaseResponse response = bot.execute(new SetGameScore(chatId, res, "AgAAAPrwAQCj_Q4D2s-51_8jsuU"));
        assertTrue(response.isOk());

        SendResponse sendResponse = (SendResponse) bot.execute(
                new SetGameScore(chatId, res + 1, chatId, 8162).force(true).disableEditMessage(true));
        GameTest.check(sendResponse.message().game());
    }

    @Test
    public void getGameHighScores() {
        GameHighScore[] scores = bot.execute(new GetGameHighScores(chatId, "AgAAAPrwAQCj_Q4D2s-51_8jsuU")).result();
        GameHighScoreTest.check(scores);

        scores = bot.execute(new GetGameHighScores(chatId, chatId, 8162)).result();
        GameHighScoreTest.check(scores);
    }

    @Test
    public void sendLocation() {
        Float lat = 21.999998f, lng = 105.2f;
        Location location = bot.execute(new SendLocation(chatId, lat, lng).livePeriod(60)).message().location();
        assertEquals(lat, location.latitude());
        assertEquals(lng, location.longitude());
    }

    @Test
    public void sendVenue() {
        Float lat = 21.999998f, lng = 105.2f;
        String title = "title", address = "addr", frsqrId = "asdfasdf", frsqrType = "frType";
        Venue venue = bot.execute(new SendVenue(chatId, lat, lng, title, address)
                .foursquareId(frsqrId)
                .foursquareType(frsqrType)
        ).message().venue();
        assertEquals(lat, venue.location().latitude());
        assertEquals(lng, venue.location().longitude());
        assertEquals(address, venue.address());
        assertEquals(title, venue.title());
        assertEquals(frsqrId, venue.foursquareId());
        assertEquals(frsqrType, venue.foursquareType());
    }

    @Test
    public void sendContact() {
        String phone = "000111", name = "first", lastName = "last", vcard = "ok vcard";
        Contact contact = bot.execute(new SendContact(chatId, phone, name).lastName(lastName).vcard(vcard)).message().contact();
        assertEquals(phone, contact.phoneNumber());
        assertEquals(name, contact.firstName());
        assertEquals(lastName, contact.lastName());
        assertEquals(vcard, contact.vcard());
        assertNull(contact.userId());
    }

    @Test
    public void deleteMessage() {
        Message message = bot.execute(new SendMessage(chatId, "message for delete")).message();
        BaseResponse response = bot.execute(new DeleteMessage(chatId, message.messageId()));
        assertTrue(response.isOk());
    }

    @Test
    public void sendChatAction() {
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.typing.name())).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.typing)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.upload_photo)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.record_video)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.upload_video)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.record_audio)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.upload_audio)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.upload_document)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.find_location)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.record_video_note)).isOk());
        assertTrue(bot.execute(new SendChatAction(chatId, ChatAction.upload_video_note)).isOk());
    }

    @Test
    public void sendVideoNote() {
        SendResponse response = bot.execute(new SendVideoNote(chatId, "DQADAgADmQADYgwpSbum1JrxPsbmAg"));
        VideoNoteCheck.check(response.message().videoNote());
    }

    @Test
    public void sendVideoNoteFile() {
        SendResponse response = bot.execute(new SendVideoNote(chatId, videoNoteFile).thumb(thumbFile).length(20).duration(30));
        VideoNoteCheck.check(response.message().videoNote(), true);
        assertNotEquals("telegram should generate thumb", thumbSize, response.message().videoNote().thumb().fileSize());

        response = bot.execute(new SendVideoNote(chatId, videoNoteBytes).thumb(thumbBytes));
        VideoNoteCheck.check(response.message().videoNote(), true);
        assertNotEquals("telegram should generate thumb", thumbSize, response.message().videoNote().thumb().fileSize());
    }

    @Test
    public void sendInvoice() {
        SendResponse response = bot.execute(new SendInvoice(chatId, "title", "desc", "my_payload",
                "284685063:TEST:NThlNWQ3NDk0ZDQ5", "my_start_param", "USD", new LabeledPrice("label", 200))
                .providerData("{\"foo\" : \"bar\"}")
                .photoUrl("https://telegram.org/img/t_logo.png").photoSize(100).photoHeight(100).photoWidth(100)
                .needPhoneNumber(true).needShippingAddress(true).needEmail(true).needName(true)
                .isFlexible(true)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("just pay").pay(),
                        new InlineKeyboardButton("google it").url("www.google.com")

                }))
        );
        InvoiceCheck.check(response.message().invoice());
        InlineKeyboardButton payButton = response.message().replyMarkup().inlineKeyboard()[0][0];
        assertTrue(payButton.isPay());
        assertEquals("just pay", payButton.text());
    }

    @Test
    public void answerShippingQuery() {
        ShippingQuery shippingQuery = getLastShippingQuery();
        String shippingQueryId = shippingQuery != null ? shippingQuery.id() : "invalid_query_id";

        BaseResponse response = bot.execute(new AnswerShippingQuery(shippingQueryId,
                new ShippingOption("1", "VNPT", new LabeledPrice("delivery", 100), new LabeledPrice("tips", 50)),
                new ShippingOption("2", "FREE", new LabeledPrice("free delivery", 0))
        ));

        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: query is too old and response timeout expired or query ID is invalid", response.description());
        }
    }

    @Test
    public void answerShippingQueryError() {
        ShippingQuery shippingQuery = getLastShippingQuery();
        String shippingQueryId = shippingQuery != null ? shippingQuery.id() : "invalid_query_id";

        BaseResponse response = bot.execute(new AnswerShippingQuery(shippingQueryId, "cant delivery so far"));

        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: query is too old and response timeout expired or query ID is invalid", response.description());
        }
    }

    private ShippingQuery getLastShippingQuery() {
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        Collections.reverse(updates);
        for (Update update : updates) {
            if (update.shippingQuery() != null) {
                return update.shippingQuery();
            }
        }
        return null;
    }

    @Test
    public void answerPreCheckoutQuery() {
        PreCheckoutQuery preCheckoutQuery = getLastPreCheckoutQuery();
        String preCheckoutQueryId = preCheckoutQuery != null ? preCheckoutQuery.id() : "invalid_query_id";

        BaseResponse response = bot.execute(new AnswerPreCheckoutQuery(preCheckoutQueryId));

        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: query is too old and response timeout expired or query ID is invalid", response.description());
        }
    }

    @Test
    public void answerPreCheckoutQueryError() {
        PreCheckoutQuery preCheckoutQuery = getLastPreCheckoutQuery();
        String preCheckoutQueryId = preCheckoutQuery != null ? preCheckoutQuery.id() : "invalid_query_id";

        BaseResponse response = bot.execute(new AnswerPreCheckoutQuery(preCheckoutQueryId, "cant sell to you"));

        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: query is too old and response timeout expired or query ID is invalid", response.description());
        }
    }

    private PreCheckoutQuery getLastPreCheckoutQuery() {
        GetUpdatesResponse updatesResponse = bot.execute(new GetUpdates());
        List<Update> updates = updatesResponse.updates();
        Collections.reverse(updates);
        for (Update update : updates) {
            if (update.preCheckoutQuery() != null) {
                return update.preCheckoutQuery();
            }
        }
        return null;
    }

    @Test
    public void exportChatInviteLink() {
        StringResponse response = bot.execute(new ExportChatInviteLink(groupId));
        assertTrue(response.isOk());
        assertNotNull(response.result());
    }

    @Test
    public void setChatPhoto() throws IOException {
        BaseResponse response = bot.execute(new SetChatPhoto(groupId, imageFile));
        assertTrue(response.isOk());

        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        response = bot.execute(new SetChatPhoto(groupId, bytes));
        assertTrue(response.isOk());
    }

    @Test
    public void deleteChatPhoto() {
        BaseResponse response = bot.execute(new DeleteChatPhoto(groupId));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: CHAT_NOT_MODIFIED", response.description());
        }
    }

    @Test
    public void setChatTitle() {
        BaseResponse response = bot.execute(new SetChatTitle(groupId, "Test Bot Group " + System.currentTimeMillis()));
        assertTrue(response.isOk());
    }

    @Test
    public void setChatDescription() {
        BaseResponse response = bot.execute(new SetChatDescription(groupId, "New desc " + System.currentTimeMillis()));
        assertTrue(response.isOk());
    }

    @Test
    public void pinChatMessage() {
        BaseResponse response = bot.execute(new PinChatMessage(groupId, 18).disableNotification(false));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: CHAT_NOT_MODIFIED", response.description());
        }
    }

    @Test
    public void unpinChatMessage() {
        BaseResponse response = bot.execute(new UnpinChatMessage(groupId));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: CHAT_NOT_MODIFIED", response.description());
        }
    }

    @Test
    public void getStickerSet() {
        GetStickerSetResponse response = bot.execute(new GetStickerSet(stickerSet));
        StickerSet stickerSet = response.stickerSet();
        for (Sticker sticker : response.stickerSet().stickers()) {
            StickerTest.check(sticker, true, true);
        }
        assertTrue(stickerSet.containsMasks());
        assertEquals(this.stickerSet, stickerSet.name());
        assertEquals("test1", stickerSet.title());

        Sticker sticker = stickerSet.stickers()[0];
        assertEquals(this.stickerSet, sticker.setName());
        MaskPosition maskPosition = sticker.maskPosition();
        assertEquals(MaskPosition.Point.forehead.name(), maskPosition.point());
        assertEquals(0f, maskPosition.xShift(), 0);
        assertEquals(0f, maskPosition.yShift(), 0);
        assertEquals(1f, maskPosition.scale(), 0);
    }

    @Test
    public void uploadStickerFile() throws IOException {
        byte[] bytes = Files.readAllBytes(stickerFile.toPath());
        GetFileResponse response = bot.execute(new UploadStickerFile(chatId, bytes));
        FileTest.check(response.file(), false);
    }

    @Test
    public void createNewStickerSet() throws IOException {
        BaseResponse response = bot.execute(
                new CreateNewStickerSet(chatId, "test" + System.currentTimeMillis() + "_by_pengrad_test_bot",
                        "test1", stickerFile, "\uD83D\uDE00")
                        .containsMasks(true)
                        .maskPosition(new MaskPosition(MaskPosition.Point.forehead, 0f, 0f, 1f)));
        assertTrue(response.isOk());
    }

    @Test
    public void addStickerToSet() {
        BaseResponse response = bot.execute(
                new AddStickerToSet(chatId, stickerSet, "BQADAgADuAAD7yupS4eB23UmZhGuAg", "\uD83D\uDE15")
                        .maskPosition(new MaskPosition("eyes", 0f, 0f, 1f)));
        assertTrue(response.isOk());
    }

    @Test
    public void setStickerPositionInSet() {
        GetStickerSetResponse setResponse = bot.execute(new GetStickerSet(stickerSet));
        Sticker sticker = setResponse.stickerSet().stickers()[0];

        BaseResponse response = bot.execute(new SetStickerPositionInSet(sticker.fileId(), 0));
        assertTrue(response.isOk());
    }

    @Test
    public void deleteStickerFromSet() {
        BaseResponse response = bot.execute(new AddStickerToSet(chatId, stickerSet, stickerFile, "\uD83D\uDE15"));
        assertTrue(response.isOk());

        GetStickerSetResponse setResponse = bot.execute(new GetStickerSet(stickerSet));
        int size = setResponse.stickerSet().stickers().length;
        Sticker sticker = setResponse.stickerSet().stickers()[size - 1];

        response = bot.execute(new DeleteStickerFromSet(sticker.fileId()));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: STICKERSET_NOT_MODIFIED", response.description());
        }
    }

    @Test
    public void editMessageLiveLocation() {
        BaseResponse response = bot.execute(new EditMessageLiveLocation(chatId, 10009, 21, 105)
                .replyMarkup(new InlineKeyboardMarkup()));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: message can't be edited", response.description());
        }

        response = bot.execute(new EditMessageLiveLocation("AgAAAPrwAQCj_Q4D2s-51_8jsuU", 21, 105));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: message is not modified", response.description());
        }
    }

    @Test
    public void stopMessageLiveLocation() {
        BaseResponse response = bot.execute(new StopMessageLiveLocation(chatId, 10009));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: message can't be edited", response.description());
        }

        response = bot.execute(new StopMessageLiveLocation("AgAAAPrwAQCj_Q4D2s-51_8jsuU"));
        if (!response.isOk()) {
            assertEquals(400, response.errorCode());
            assertEquals("Bad Request: message is not modified: specified new message content and reply markup are exactly the same as a current content and reply markup of the message",
                    response.description());
        }
    }

    @Test
    public void setChatStickerSet() {
        BaseResponse response = bot.execute(new SetChatStickerSet(groupId, "PengradTest"));
        assertFalse(response.isOk());
        assertEquals(400, response.errorCode());
    }

    @Test
    public void deleteChatStickerSet() {
        BaseResponse response = bot.execute(new DeleteChatStickerSet(groupId));
        assertFalse(response.isOk());
        assertEquals(400, response.errorCode());
    }

    @Test
    public void sendMediaGroup() {
        MessagesResponse response = bot.execute(new SendMediaGroup(chatId,
                new InputMediaPhoto(photoFileId),
                new InputMediaPhoto(imageFile).caption("some caption <b>bold</b>").parseMode(ParseMode.HTML),
                new InputMediaPhoto(imageBytes),
                new InputMediaVideo(videoFileId),
                new InputMediaVideo(videoFile),
                new InputMediaVideo(videoBytes).caption("my video <b>bold</b>").parseMode(ParseMode.HTML)
                        .duration(10).width(11).height(12).supportsStreaming(true)
        ));
        assertTrue(response.isOk());
        assertEquals(6, response.messages().length);

        String mediaGroupId = response.messages()[0].mediaGroupId();
        assertNotNull(mediaGroupId);

        int messagesWithCaption = 0;
        for (Message message : response.messages()) {
            assertEquals(mediaGroupId, message.mediaGroupId());
            if (message.caption() != null) {
                assertEquals(MessageEntity.Type.bold, message.captionEntities()[0].type());
                messagesWithCaption++;
            }
        }
        assertEquals(2, messagesWithCaption);
    }

    @Test
    public void editMessageMedia() {
        int messageId = 13541;
        SendResponse response;

        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId,
                new InputMediaDocument(docFile).thumb(thumbFile)));
        assertEquals((Integer) 14, response.message().document().fileSize());
        assertEquals(thumbSize, response.message().document().thumb().fileSize());

        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId,
                new InputMediaDocument(docBytes).thumb(thumbBytes)));
        assertEquals((Integer) 14, response.message().document().fileSize());
        assertEquals(thumbSize, response.message().document().thumb().fileSize());

        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId, new InputMediaDocument(docFileId)));
        MessageTest.checkMessage(response.message());
        DocumentTest.check(response.message().document());


        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId, new InputMediaAnimation(gifFile)));
        assertEquals(new Integer(1), response.message().animation().duration());

        Integer durationAnim = 17, width = 21, height = 22;
        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId,
                new InputMediaAnimation(gifBytes).duration(durationAnim).width(width).height(height)
        ));
        Animation animation = response.message().animation();
        assertEquals(Integer.valueOf(1), animation.duration());
        assertEquals(width, animation.width());
        assertEquals(height, animation.height());

        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId, new InputMediaAnimation(gifFileId)));
        assertTrue(response.isOk());
        assertEquals(Integer.valueOf(3), response.message().animation().duration());
        assertEquals(gifFileId, response.message().animation().fileId());
        assertNotNull(response.message().document());
        assertEquals((Integer) 57527, response.message().document().fileSize());
        assertEquals("video/mp4", response.message().document().mimeType());


        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId, new InputMediaAudio(audioFile)));
        assertEquals((Integer) 10286, response.message().audio().fileSize());
        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId, new InputMediaAudio(audioBytes)));
        assertEquals((Integer) 10286, response.message().audio().fileSize());
        Integer duration = 34;
        String performer = "some performer", title = "just a title";
        response = (SendResponse) bot.execute(new EditMessageMedia(chatId, messageId,
                new InputMediaAudio(audioFile).duration(duration).performer(performer).title(title)
        ));
        Audio audio = response.message().audio();
        assertEquals((Integer) 10286, audio.fileSize());
        assertEquals(duration, audio.duration());
        assertEquals(performer, audio.performer());
        assertEquals(title, audio.title());
    }

    @Test
    public void sendAnimation() {
        Integer width = 340, height = 240;
        String caption = "gif *file*", captionCheck = "gif file";
        SendResponse response = bot.execute(new SendAnimation(chatId, gifFile)
                .duration(222).width(width).height(height).thumb(thumbFile)
                .caption(caption).parseMode(ParseMode.Markdown));
        assertTrue(response.isOk());
        Animation animation = response.message().animation();
        assertEquals((Integer) 1, animation.duration());
        assertEquals(width, animation.width());
        assertEquals(height, animation.height());
        assertNotEquals("telegram should generate thumb", thumbSize, animation.thumb().fileSize());
        assertEquals(captionCheck, response.message().caption());
        assertEquals(MessageEntity.Type.bold, response.message().captionEntities()[0].type());

        response = bot.execute(new SendAnimation(chatId, gifBytes).thumb(thumbBytes));
        animation = response.message().animation();
        assertEquals((Integer) 1, animation.duration());
        assertEquals((Integer) 160, animation.width());
        assertEquals((Integer) 160, animation.height());
        assertNotEquals("telegram should generate thumb", thumbSize, animation.thumb().fileSize());

        response = bot.execute(new SendAnimation(chatId, gifFileId));
        animation = response.message().animation();
        assertEquals((Integer) 3, animation.duration());
        assertEquals((Integer) 128, animation.width());
        assertEquals((Integer) 128, animation.height());
    }

    @Test
    public void setPassportDataErrors() {
        BaseResponse response = bot.execute(new SetPassportDataErrors(chatId,
                new PassportElementErrorDataField("personal_details", "first_name",
                        "TueU2/SswOD5wgQ6uXQ62mJrr0Jdf30r/QQ/jyETHFM=",
                        "error in page 1")
        ));
        System.out.println(response);
        assertTrue(response.isOk());
    }

    @Test
    public void decryptPassport() throws Exception {
        PassportData passportData = BotUtils.parseUpdate(testPassportData).message().passportData();
        assertNotNull(passportData);

        Credentials credentials = passportData.credentials().decrypt(privateKey);
        assertNull(credentials.nonce());

        SecureData secureData = credentials.secureData();
        assertNotNull(secureData.personalDetails());
        assertNull(secureData.internalPassport());
        assertNull(secureData.driverLicense());
        assertNull(secureData.identityCard());
        assertNull(secureData.address());
        assertNull(secureData.utilityBill());
        assertNull(secureData.bankStatement());
        assertNull(secureData.rentalAgreement());
        assertNull(secureData.passportRegistration());
        assertNull(secureData.temporaryRegistration());

        SecureValue securePassport = secureData.passport();
        assertNull(securePassport.reverseSide());
        assertNull(securePassport.selfie());
        assertNull(securePassport.files());

        for (EncryptedPassportElement encElement : passportData.data()) {
            assertNotNull(encElement.data());
            if (encElement.type() == EncryptedPassportElement.Type.personal_details) {
                assertEquals("DVUCaJq6oU/hItqZjuclmKL1bWwMSACR9w0Kx8PjoHg=", encElement.hash());
                assertNull(encElement.phoneNumber());
                assertNull(encElement.email());
                PersonalDetails pd = (PersonalDetails) encElement.decryptData(credentials);
                assertEquals("Sz2", pd.firstName());
                assertEquals("P", pd.lastName());
                assertEquals("smid", pd.middleName());
                assertEquals("1.1.1980", pd.birthDate());
                assertEquals("male", pd.gender());
                assertEquals("RU", pd.countryCode());
                assertEquals("RU", pd.residenceCountryCode());
                assertEquals("имя", pd.firstNameNative());
                assertEquals("фамилия", pd.lastNameNative());
                assertEquals("среднее", pd.middleNameNative());
            }

            if (encElement.type() == EncryptedPassportElement.Type.passport) {
                assertEquals(Integer.valueOf(260608), encElement.frontSide().fileSize());
                assertEquals(Integer.valueOf(1535386777), encElement.frontSide().fileDate());

                List<PassportFile> files = new ArrayList<>();
                files.add(encElement.frontSide());
                files.add(encElement.reverseSide());
                files.add(encElement.selfie());
                if (encElement.files() != null) {
                    files.addAll(Arrays.asList(encElement.files()));
                }
                if (encElement.translation() != null) {
                    files.addAll(Arrays.asList(encElement.translation()));
                }
                for (int i = 0; i < files.size(); i++) {
                    PassportFile file = files.get(i);
                    if (file == null) continue;
                    byte[] data = encElement.decryptFile(file, credentials, bot);
                    assertTrue(data.length > 0);
                    // new FileOutputStream(Paths.get("build/" + encElement.type() + i + ".jpg").toFile()).write(data);
                }
            }
        }
    }

    @Test
    public void sendPoll() {
        String question = "Question ?";
        String[] answers = {"Answer 1", "Answer 2"};
        SendResponse sendResponse = bot.execute(new SendPoll(groupId, question, answers));
        Poll poll = sendResponse.message().poll();
        assertFalse(poll.isClosed());
        assertEquals(question, poll.question());
        assertEquals(answers.length, poll.options().length);
        for (int i = 0; i < answers.length; i++) {
            PollOption option = poll.options()[i];
            assertEquals(answers[i], option.text());
            assertEquals(Integer.valueOf(0), option.voterCount());
        }
    }

    @Test
    public void stopPoll() throws InterruptedException {
        String question = "Question ?";
        String[] answers = {"Answer 1", "Answer 2"};
        SendResponse sendResponse = bot.execute(new SendPoll(groupId, question, answers));
        Integer messageId = sendResponse.message().messageId();

        Thread.sleep(1000);

        PollResponse response = bot.execute(new StopPoll(groupId, messageId));
        Poll poll = response.poll();
        assertTrue(poll.isClosed());
        assertEquals(question, poll.question());
        assertEquals(answers.length, poll.options().length);
        for (int i = 0; i < answers.length; i++) {
            PollOption option = poll.options()[i];
            assertEquals(answers[i], option.text());
            assertEquals(Integer.valueOf(0), option.voterCount());
        }
    }

    @Test
    public void testAsyncCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        bot.execute(new GetMe(), new Callback<GetMe, GetMeResponse>() {
            @Override
            public void onResponse(GetMe request, GetMeResponse response) {
                latch.countDown();
            }

            @Override
            public void onFailure(GetMe request, IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void botClientError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        TelegramBotClient botClient = new TelegramBotClient(new OkHttpClient(), null, TelegramBot.Builder.API_URL);
        botClient.send(new GetMe(), new Callback<GetMe, BaseResponse>() {
            @Override
            public void onResponse(GetMe request, BaseResponse response) {
            }

            @Override
            public void onFailure(GetMe request, IOException e) {
                latch.countDown();
            }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void toWebhookResponse() {
        assertEquals("{\"method\":\"getMe\"}", new GetMe().toWebhookResponse());
    }

    @Test
    public void loginButton() {
        String text = "login";
        String url = "http://pengrad.herokuapp.com/hello";
        SendResponse response = bot.execute(
                new SendMessage(chatId, "Login button").replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton(text).loginUrl(new LoginUrl(url)
                                .forwardText("forwarded login")
                                .botUsername("pengrad_test_bot")
                                .requestWriteAccess(true))
                })));
        assertTrue(response.isOk());
        InlineKeyboardButton button = response.message().replyMarkup().inlineKeyboard()[0][0];
        assertEquals(text, button.text());
        assertEquals(url, button.url());
    }

    @Test
    public void multipartNonAscii() {
        String caption = "хорошо";
        Message message = bot.execute(
                new SendPhoto(chatId, imageFile).fileName("файл.txt").caption(caption)
        ).message();
        assertEquals(caption, message.caption());
        MessageTest.checkMessage(message);
        PhotoSizeTest.checkPhotos(message.photo());
    }
}
