const admin = require("firebase-admin");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");

admin.initializeApp();

exports.sendNotification = onDocumentCreated(
  "notifications/{notificationId}",
  async (event) => {

    const data = event.data.data();

    const senderOwnerId = data.ownerId;

    const snapshot = await admin
      .firestore()
      .collection("deviceTokens")
      .get();

    const tokens = [];

    snapshot.forEach((doc) => {

      const tokenData = doc.data();

      if (tokenData.ownerId !== senderOwnerId) {
        tokens.push(tokenData.token);
      }
    });

    if (tokens.length === 0) {
      return;
    }

       const response =
        await admin.messaging().sendEachForMulticast({
          tokens,
          data: {
            title: data.title,
            body: data.message,
          },
        });

       console.log(
         "Success:",
         response.successCount
       );

       console.log(
         "Failure:",
         response.failureCount
       );

       response.responses.forEach(
         (result, index) => {

           if (!result.success) {

             console.log(
               "Token inválido:",
               tokens[index]
             );
           }
         }
       );


    console.log(
      "Notificación enviada a",
      tokens.length,
      "dispositivos"
    );
  }
);