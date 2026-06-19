<?php if (!defined('BASEPATH')) exit('No direct script access allowed');
return array(
  'components' => array(
    'db' => array(
      'connectionString' => 'mysql:host=lime-db;port=3306;dbname=limesurvey;',
      'emulatePrepare' => true,
      'username' => 'limesurvey',
      'password' => 'limesurvey',
      'charset' => 'utf8mb4',
      'tablePrefix' => 'lime_',
    ),
    //'session' => array (
    //   'class' => 'application.core.web.DbHttpSession',
    //   'connectionID' => 'db',
    //   'sessionTableName' => '{{sessions}}',
    //),
    'urlManager' => array(
      'urlFormat' => 'path',
      'rules' => array(),
      'showScriptName' => true,
    ),

  ),
  'config'=>array(
    'publicurl'=>'',
    'debug'=>0,
    'debugsql'=>0,
    'mysqlEngine' => 'MyISAM',
  )
);

