<?php
$servername = "localhost"; //replace it with your database server name
$username = "user";  //replace it with your database username
$password = "password";  //replace it with your database password
$dbname = "databasename";
// Create connection
$conn = mysqli_connect($servername, $username, $password, $dbname);
// Check connection
if (!$conn) {
    die("Connection failed: " . mysqli_connect_error());
}
?>