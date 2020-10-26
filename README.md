# lighthouse-talos

[lighthouse-talos](./) is a collection of Spring filters that can be configured
to replace certain Lil' Kong plugins and add an extra layer of protection to 
Spring applications.

Example configurations for the following filters can be found 
[here](./src/test/java/gov/va/api/lighthouse/talos/fugazi/FugaziConfig.java).

### Available Filters
- [Client-Key Protected Endpoint](#client-key-protected-endpoint)

### Client-Key Protected Endpoint

The client-key protected endpoint filter can be used to add static token protection
to specific endpoints in Spring applications. If an endpoint configured with this
filter receives a request containing a token header that does not match a value within the 
list of valid tokens, the request will be rejected.

##### Configurable Attributes
- clientKeyHeader  `OPTIONAL`
    - The name of the header where the filter expects the key value to exist.
    - Default: `client-key`
- clientKeys `REQUIRED`
    - A list of strings that represent the valid client-keys for the endpoint configured
    with this filter. 
- unauthorizedReponse `REQUIRED`
    - The configurable HttpServletResponse for an unauthorized/invalid client-key value.
- name `OPTIONAL`
  - The name of the endpoint. This will be logged if a client-key is invalid or unauthorized.
  - Default: `Client key protected endpoint`
