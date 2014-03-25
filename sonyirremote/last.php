<?php
$file = "users.ini";
$ip = $_SERVER['REMOTE_ADDR'];
$time = time();
$content = @file_get_contents($file);
$new_content = $ip." = ".$time . " url : $_SERVER[HTTP_HOST] $_SERVER[REQUEST_URI]";
$content .= $new_content."\r\n";
@file_put_contents($file,$content);

?>

<?php
 
  $apks = scandir("/var/www/sonyirremote/apk", 1);
  $apk = substr($apks[0], 14, 20); 
  $last = substr($apk, 0, -4);
  echo $last;
?>



















