<html>
<meta charset="UTF-8">
<link rel="shortcut icon" href="http://www.pocketmine.net/favicon.png" />
<head><script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-44543019-1', 'or.gs');
  ga('send', 'pageview');

</script>
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-46126057-1']);
  _gaq.push(['_setDomainName', 'sssemil.tk']);
  _gaq.push(['_setAllowLinker', true]);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
<?php
$file = "/var/www/sonyirremote/users.ini";
$ip = $_SERVER['REMOTE_ADDR'];
$time = date('Y-m-d');
$content = @file_get_contents($file);
$new_content = $ip." = ".$time . " url : $_SERVER[HTTP_HOST] $_SERVER[REQUEST_URI]";
$content .= $new_content."\r\n";
@file_put_contents($file,$content);

?>

<?php
$v=$_GET['v'];
if ($v == "last") {  
  $apks = scandir("/var/www/sonyirremote/apk", 1);
  print_r($apks);
  header("Location: apk/" . $apks[0]);  
}
else {
  $file = "/var/www/sonyirremote/downloads.ini";
  $ip = $_SERVER['REMOTE_ADDR'];
  $time = time();
  $content = @file_get_contents($file);
  $new_content = $ip." = ".$time . " url : $_SERVER[HTTP_HOST] $_SERVER[REQUEST_URI]";
  $content .= $new_content."\r\n";
  @file_put_contents($file,$content);

  header("Location: apk/SonyIRRemote-v" . $v . ".apk");
}
?>



















