<?php
define( '_topLevelPage', 1 );
require_once 'include/common.php';

if (!empty($wp_user)) {
    $conn = new mysqli($wp_host, $wp_user, $wp_pass, $wp_schema, $db_port);
    $result = query($conn, "select post_name, post_modified from wp_posts where post_status=?", "s", "publish");
}
else {
    $result = array();
}

header("Content-type: text/xml");
echo '<?xml version="1.0" encoding="UTF-8"?>';
?>

<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/home</loc>
    <lastmod>2009-12-14</lastmod>
    <changefreq>daily</changefreq>
    <priority>1.0</priority>
  </url>  
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/help</loc>
    <lastmod>2009-12-14</lastmod>
    <changefreq>monthly</changefreq>
    <priority>1.0</priority>
  </url>  
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/tasks</loc>
    <lastmod>2009-12-14</lastmod>
    <changefreq>daily</changefreq>
    <priority>0.7</priority>
  </url>  
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/blog/</loc>
    <lastmod><?php echo date('Y-m-d'); ?></lastmod>
    <changefreq>daily</changefreq>
    <priority>1.0</priority>
  </url>
<?php foreach ($result as $row) { ?>  
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/blog/<?php echo $row['post_name']; ?></loc>
    <lastmod><?php echo date('Y-m-d', strtotime($row['post_modified'])); ?></lastmod>
    <changefreq>weekly</changefreq>
    <priority>1.0</priority>
  </url>
<?php } ?>    
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/register</loc>
    <lastmod>2009-12-14</lastmod>
    <changefreq>monthly</changefreq>
    <priority>0.2</priority>
  </url>  
  <url>
    <loc>http://<?php echo $_SERVER['HTTP_HOST']; ?>/login</loc>
    <lastmod>2009-12-14</lastmod>
    <changefreq>monthly</changefreq>
    <priority>0.1</priority>
  </url>
</urlset>
