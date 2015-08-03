<?php 
defined('_topLevelPage') or die('Restricted access');
if (empty($title))
    $title = 'The Game of Intelligent Design';
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
  <title><?php echo $title; ?></title>
  <link rel="SHORTCUT ICON" href="/favicon.ico"/>
  
  <?php if ($_SERVER['HTTP_HOST'] == 'gameofid.com') { ?>
    <meta name="google-site-verification" content="6YFuMaTq8fq_FFdkRlLcax8GZMEvvxQmOHD0kj9O8gI" />
  <?php } elseif ($_SERVER['HTTP_HOST'] == 'gameofintelligentdesign.com') { ?>
  <meta name="google-site-verification" content="cl5si_HtJz2Jflj8tu2To8lBEF4z5Ff_EjmxGmbcu00" />
  <?php } ?>
  <meta name="description" content="The blog for the Game of Intelligent Design, a game about the development of an Artificial General Intelligence blog" />
  <meta name="keywords" content="Game of Intelligent Design, Artificial General Intelligence, AI, AGI, game, Intelligent Design" />
  
  <link type="text/css" rel="stylesheet" href="/res/main.css" />
  <script type="text/javascript" src="/res/jquery-1.3.2.min.js"></script>
  <script type="text/javascript" src="/res/main.js"></script>
</head>
<body>