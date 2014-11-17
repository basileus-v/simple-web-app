<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<o:header title="Fhir"/>
<o:topbar pageName="Fhir"/>
<div class="container-fluid main">
    <div class="row-fluid">
        <div class="span10 offset1">
            <c:if test="${not empty fhirResource.resourceUrl}">
                <h1>Result of quering ${fhirResource.resourceUrl}</h1>

                <h2>Status: ${ status }</h2>

                <h2>Response:</h2>

                <div>
                    <pre>${ response }</pre>
                </div>
            </c:if>
            <form:form method="post" modelAttribute="fhirResource">
                <form:label path="resourceUrl">Resource URL</form:label>
                <form:input path="resourceUrl" cssStyle="width: 400px;"/>
                <br/>

                <input type="submit" value="Submit"/>
            </form:form>
        </div>
    </div>
</div>
<o:footer/>
