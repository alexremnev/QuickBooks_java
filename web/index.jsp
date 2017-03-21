<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title style="align-content: center"> Welcome to the QuickBooks application</title>
      <script type="text/javascript" src="https://appcenter.intuit.com/Content/IA/intuit.ipp.anywhere-1.3.2.js">
      </script>
      <script>intuit.ipp.anywhere.setup({
          menuProxy: '',
          grantUrl: 'http://localhost:8080/start_oauth.htm'});
      </script>
  </head>
  <body>
    Welcome to my application. If you want to connect to QuickBooks - click on button below!<br>
    <ipp:connectToIntuit></ipp:connectToIntuit>
    <form method="post" action="calculate.htm">
        <input type="submit" value="Calculate"/>
    </form>
    <form method="post" action="save.htm">
        <input type="submit" value="Save"/>
    </form>
     </body>
</html>
