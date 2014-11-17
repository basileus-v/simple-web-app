/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.mitre.web;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mitre.openid.connect.client.OIDCAuthenticationFilter;
import org.mitre.openid.connect.client.SubjectIssuerGrantedAuthority;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  // filter reference so we can get class names and things like that.
  @Autowired
  private OIDCAuthenticationFilter filter;

  @Resource(name = "namedAdmins")
  private Set<SubjectIssuerGrantedAuthority> admins;

  /**
   * Simply selects the home view to render by returning its name.
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String home(Locale locale, Model model, Principal p) {

    model.addAttribute("issuerServiceClass", filter.getIssuerService().getClass().getSimpleName());
    model.addAttribute("serverConfigurationServiceClass", filter.getServerConfigurationService().getClass().getSimpleName());
    model.addAttribute("clientConfigurationServiceClass", filter.getClientConfigurationService().getClass().getSimpleName());
    model.addAttribute("authRequestOptionsServiceClass", filter.getAuthRequestOptionsService().getClass().getSimpleName());
    model.addAttribute("authRequestUriBuilderClass", filter.getAuthRequestUrlBuilder().getClass().getSimpleName());

    model.addAttribute("admins", admins);

    return "home";
  }

  @RequestMapping("/user")
  @PreAuthorize("hasRole('ROLE_USER')")
  public String user(Principal p) {
    return "user";
  }

  @RequestMapping(value = "/fhir")
  public String fhir(FhirResource fhirResource, Model model) {
    if (fhirResource.getResourceUrl() == null) {
      fhirResource.setResourceUrl("http://localhost:8181/cxf/Patient");
    }
    model.addAttribute("fhirResource", fhirResource);
    if (fhirResource != null && fhirResource.getResourceUrl() != null && fhirResource.getResourceUrl().startsWith("http")) {
      queryFhirServer(fhirResource, model);
    }
    return "fhir";
  }

  private void queryFhirServer(FhirResource fhirResource, Model model) {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(fhirResource.getResourceUrl());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof OIDCAuthenticationToken) {
      OIDCAuthenticationToken token = ((OIDCAuthenticationToken) authentication);
      httpGet.addHeader("Authorization", "Bearer " + token.getAccessTokenValue());
    }
    try {
      HttpResponse response = httpClient.execute(httpGet);
      int status = response.getStatusLine().getStatusCode();
      model.addAttribute("status", status);
      model.addAttribute("response", status == 200 ? getResponse(response) : null);
    } catch (IOException e) {
    }
  }


  private String getResponse(HttpResponse response) throws IOException {
    String rawResponse = IOUtils.toString(response.getEntity().getContent(), "utf8");
    return StringEscapeUtils.escapeXml11(rawResponse);
  }

  @RequestMapping("/admin")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public String admin(Model model, Principal p) {

    model.addAttribute("admins", admins);

    return "admin";
  }

  @RequestMapping("/login")
  public String login(Principal p) {
    return "login";
  }

  public static class FhirResource {
    private String resourceUrl;

    public String getResourceUrl() {
      return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
      this.resourceUrl = resourceUrl;
    }
  }

}
