--
-- Tasks
create table tasks (
  id varchar(10) not null,
  name varchar(40) not null,
  classname varchar(50) not null,
  priority int not null,
  level int not null,
  authorName varchar(30) not null,
  authorLink varchar(128),
  
  primary key (id)
);

insert into tasks (id, name, classname, priority, level, authorName, authorLink) values
  ('donut', 'The donut run', 'com.serotonin.goid.task.donut.DonutTask', 1, 1, 'mlohbihler', null),
  ('collector', 'The collector', 'com.serotonin.goid.task.collector.CollectorTask', 2, 2, 'mlohbihler', null),
  ('roboarm', 'Robotic arm', 'com.serotonin.goid.task.arm.ArmTask', 3, 3, 'mlohbihler', null),
  ('maze', 'Maze', 'com.serotonin.goid.task.maze.MazeTask', 4, 3, 'mharms', 'http://raelifin.com')
  ;

--
-- Users
create table users (
  id char(20) not null,
  username varchar(20) not null,
  email varchar(50) not null,
  password varchar(136) not null,
  verificationToken char(5) not null,
  emailVerified boolean not null,
  splitLocation int not null,
  outputSplitLocation int not null,
  frameInfo varchar(128),
  creationDate int not null,
  remoteAddr varchar(30),
  location varchar(100),
  notifyOnNewTask boolean not null,
  notifyOnBeatenTopRank boolean not null,
  goidRank int,
  
  primary key (id),
  unique (username),
  unique (email)
);

--
-- User tasks
create table userTasks (
  userId char(20) not null,
  taskId varchar(10) not null,
  script mediumtext,
  completed boolean not null,
  score int,
  resultDetails text,
  rank int,
  lastCompletion int,
  lastUpdated int not null,
  
  primary key (userId, taskId),
  foreign key (userId) references users(id),
  foreign key (taskId) references tasks(id)
);



--
-- Testing tables
drop table rankTest;
create table rankTest (
  uid int,
  tid char(1),
  rank int
);

insert into rankTest (uid, tid, rank) values
  (1, 'a', 1),
  (1, 'b', 2),
  (1, 'e', 1),
  (2, 'a', 3),
  (2, 'c', 4),
  (2, 'e', 1),
  (3, 'a', 2),
  (3, 'b', 1),
  (3, 'e', 2),
  (4, 'c', 1),
  (4, 'e', 2),
  (5, 'c', 2),
  (5, 'e', 2),
  (6, 'c', 2),
  (6, 'e', 1),
  (8, 'a', 1),
  (8, 'b', 2),
  (8, 'c', 3),
  (8, 'd', 4),
  (8, 'e', 5),
  (8, 'f', 5),
  (9, 'd', 1);
