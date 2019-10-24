<?php
$noMessage = "Welcome To AfriRise,\nDue to large amount of messages, please contact customer service through hello@afririse.com for general inquieries.\nTo receive opportunities on your messenger, please write a message starting with the word rise, followed by information category e.g business, innovation, jobs etc. You will receive the latest posts on www.afririse.com.";

   include_once("connection.php");

/* validate verify token needed for setting up web hook */ 
if (isset($_GET['hub_verify_token'])) { 
    if ($_GET['hub_verify_token'] === 'your_token') {
        echo $_GET['hub_challenge'];
        return;
    } else {
        echo 'Invalid Verify Token';
        return;
    }
}

/* receive and send messages */
$input = json_decode(file_get_contents('php://input'), true);
if (isset($input['entry'][0]['messaging'][0]['sender']['id'])) {
    
    
    $sender = $input['entry'][0]['messaging'][0]['sender']['id']; //sender facebook id
    $message = $input['entry'][0]['messaging'][0]['message']['text']; //text that user sent

    $url = 'https://graph.facebook.com/v2.6/me/messages?access_token=YOUR_VERIFICATION_TOKEN';
    
    $responding = "No such category was found! Please try again...";
    
    
    if(strpos($message, 'rise')!==false || strpos($message, 'Rise')!==false|| strpos($message, 'RISE')!==false){
        
         $query = "SELECT * FROM your_data_table ORDER BY ID DESC LIMIT 3"; 
    
    $result = mysqli_query($conn, $query);
    
    $count = 1;
    
    while($row = mysqli_fetch_assoc($result)){
           // $data[] = $row;
           
           //if(strpos($message, $row['p_code'])!==false){
               $responding = $count.". ";
               $responding  .= "Post Title : ".$row['post_title'].",  ";
               //$responding  .= "Description : ".$row['post_excerpt'].",  ";
               
               $responding .= "Link : ". $row['guid'];
               $responding  .= "\n\n";
               
               
          // }
    }
    //echo json_encode($data);
    $responding  .= "\nThank You For Using AfriRise, inform | arise";


    /*initialize curl*/
    $ch = curl_init($url);
    /*prepare response*/
    $jsonData = '{
    "recipient":{
        "id":"' . $sender . '"
        },
        "message":{
            "text":"' . $responding . '"
        }
    }';
    
      /* curl setting to send a json post data */
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
    if (!empty($message)) {
        $result = curl_exec($ch); // user will get the message
    }
    }
    else {
       
        /*initialize curl*/
    $ch = curl_init($url);
    /*prepare response*/
    $jsonData = '{
    "recipient":{
        "id":"' . $sender . '"
        },
        "message":{
            "text":"' . $responding . '"
        }
    }';
    
      /* curl setting to send a json post data */
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $jsonData);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array('Content-Type: application/json'));
    if (!empty($message)) {
        $result = curl_exec($ch); // user will get the message
    }
    }
  
}

?>