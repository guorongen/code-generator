<!DOCTYPE html>
<html>
<head>
    <title>百度官网</title>
</head>
<body>
<h1>欢迎来到百度官网</h1>
<ul>
    <#list menuItems as item>
        <li><a href="${item.url}">${item.label}</a> </li>
    </#list>
</ul>
<footer>
    ${currentYear} 百度官网. All rights reserved.
</footer>
</body>
</html>