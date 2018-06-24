package com.cd.springbootdemo.mybatisplusdemo.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhongan.plutus.exception.PlutusException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;

/**
 * http请求工具类
 */
public class HttpClientUtil {
    private final static Logger                       logger                = LoggerFactory
                                                                                    .getLogger(HttpClientUtil.class);

    //  private static final HttpClient client         = new HttpClient(new SimpleHttpConnectionManager());
    private static MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
    private static HttpClient                         client;
    
    static HttpClientBuilder httpClientBuilder;
    
    static {
        //每主机最大连接数
        Protocol myhttps = new Protocol("https", new MySecureProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", myhttps);
        client = new HttpClient(httpConnectionManager);
        client.getHttpConnectionManager().getParams().setDefaultMaxConnectionsPerHost(32);
        //总最大连接数
        client.getHttpConnectionManager().getParams().setMaxTotalConnections(256);
        //超时时间 3sec
        client.getHttpConnectionManager().getParams().setConnectionTimeout(6000);
        client.getHttpConnectionManager().getParams().setSoTimeout(6000);
        //client.getHttpConnectionManager().getParams().setTcpNoDelay(true);
        //client.getHttpConnectionManager().getParams().setLinger(1000);
        
        
        httpClientBuilder=HttpClientBuilder.create();
        httpClientBuilder.setMaxConnTotal(32);

    }

    public static HttpClient getHttpClient() {
        return client;
    }
    
    /**
     * POST发送JSON
     * @param uri
     * @param content
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public static String doRequestPostJson(String uri, String content) {
		String result = null;
		PostMethod postMethod = null;
		try {
			postMethod = new PostMethod(uri);
			RequestEntity entity = new StringRequestEntity(content, ContentType.APPLICATION_JSON.getMimeType(), Consts.UTF_8.displayName());
			postMethod.addRequestHeader("Accept", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
		    postMethod.setRequestEntity(entity);
		    
		    String json="{\"product_code\": \"wy-za-sandbox-0001\",   \"user_id\": \"101\",   \"bank_account\": \"666666612345258223\",   \"user_name\": \"中国\",   \"identity_type\": 0,   \"identity_no\": \"130123196709096561\",   \"phone\": \"18758585646\", \"bank_id\": \"TEST\",   \"bank_card_type\": 1,   \"valid_date\": \"201610\", \"user_ip\": \"192.168.0.1\" }";
		    NameValuePair [] nameValue={new NameValuePair("version", "1.0")
		    ,new NameValuePair("request_id", "12345678-1234-1234-1234-201607251919")
		    ,new NameValuePair("app_id", "X9qY7gry76gMhd5n2JPf6KN1GEzsebzk")
		    ,new NameValuePair("method", "wyxd.merchant.card.bind")
		    ,new NameValuePair("timestamp", LocalDateTime.now().toString())
		    ,new NameValuePair("format", "json")
		    ,new NameValuePair("sign_method", "md5")
		    ,new NameValuePair("sign", "ttttest")
		    ,new NameValuePair("data", new String(json.getBytes("UTF-8")))};
		    postMethod.setRequestBody(nameValue);
            int statusCode = getHttpClient().executeMethod(postMethod);
            if(statusCode != HttpStatus.SC_OK){
                logger.error("doRequestPostJson server return not equals 200, status = {}, uri = {}", statusCode, uri);
                throw new PlutusException("HttpClientUtil.doRequestPostJson call server return no 200, status：" + statusCode);
            }
            byte[] responseBody = postMethod.getResponseBody();
			result = new String(responseBody, CharEncoding.UTF_8);
		} catch (Exception e) {
            logger.warn("post request exception occurs.", e);
			throw new PlutusException("POST请求失败", e);
		} finally {
			if (null != postMethod) {
				postMethod.releaseConnection();
			}
		}
		return result;	
    }
    
    public static String doRequestPostJson2(String uri, Map<String, Object> dataMap) {
    	CloseableHttpClient closeableHttpClient=null;
        HttpPost httpPost = new HttpPost(uri);
        try {
        	 closeableHttpClient=httpClientBuilder.build();
            // 设置请求的header
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            List<BasicNameValuePair> formparams = generateParamValues(dataMap);
            UrlEncodedFormEntity uefEntity;
            // 执行请求
            HttpResponse httpResponse;
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(uefEntity);
            httpResponse = closeableHttpClient.execute(httpPost);
            // 获取响应消息实体
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if ( statusCode != HttpStatus.SC_OK) {
                logger.warn("post response not ok, status code = {}, uri = {}", statusCode, uri);
                throw new PlutusException(String.format("post请求返回结果不正常，状态码为%s", statusCode));
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String entityStr = EntityUtils.toString(entity, "utf-8");
                logger.info("post request response:{}", entityStr);
                return entityStr;
            } else {
                logger.error("post request response, entity is  null.");
                throw new PlutusException("post请求返回实体为空.");
            }
        } catch (Exception e) {
            logger.error("post request exception.", e);
            throw new PlutusException(e);
        }finally{
            if(closeableHttpClient!=null){
            	try {
                    httpPost.releaseConnection();
					closeableHttpClient.close();
				} catch (IOException e) {
                    logger.error("", e);
				}
            }
        }
    }

    /**
     * 发送post请求
     * @param uri
     * @param dataMap 请求报文体
     * @return
     */
    public static String postRequest(String uri, Map<String, Object> dataMap) {
        CloseableHttpClient closeableHttpClient=null;
        HttpPost httpPost = new HttpPost(uri);
        try {
            closeableHttpClient = httpClientBuilder.build();
            // 执行请求
            HttpResponse httpResponse;
            StringEntity stringEntity = new StringEntity(JSON.toJSONString(dataMap), CharEncoding.UTF_8);
            stringEntity.setContentType(ContentType.APPLICATION_JSON.toString());
            httpPost.setEntity(stringEntity);
            httpResponse = closeableHttpClient.execute(httpPost);
            // 获取响应消息实体
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if ( statusCode != HttpStatus.SC_OK) {
                logger.warn("post response not ok, status code = {}, uri = {}", statusCode, uri);
                throw new PlutusException(String.format("post请求返回结果不正常，状态码为%s", statusCode));
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                String entityStr = EntityUtils.toString(entity, "utf-8");
                logger.info("post request response:{}", entityStr);
                return entityStr;
            } else {
                logger.error("post request response, entity is  null.");
                throw new PlutusException("post请求返回实体为空.");
            }
        } catch (Exception e) {
            logger.error("post request exception.", e);
            throw new PlutusException(e);
        }finally{
            if(closeableHttpClient!=null){
                try {
                    httpPost.releaseConnection();
                    closeableHttpClient.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }
    
    /**
     * GET发送JSON
     * @param uri
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public static String doRequestGetJson(String uri, Map<String, Object> urlParamMap) {
        List<BasicNameValuePair> basicNameValuePairs = generateParamValues(urlParamMap);
		String result = null;
		GetMethod getMethod  = null;
		try {
			getMethod = new GetMethod(uri + "?" + URLEncodedUtils.format(basicNameValuePairs, "UTF-8"));
			getMethod.addRequestHeader("Accept", ContentType.APPLICATION_JSON.toString());
			int statusCode = getHttpClient().executeMethod(getMethod);
            if(statusCode != HttpStatus.SC_OK){
                logger.error("doRequestGetJson server return not equal 200, status = {}, uri={}", statusCode, uri);
                throw new PlutusException("HttpClientUtil.doRequestGetJson call server return no 200, status：" + statusCode);
            }
            byte[] responseBody = getMethod.getResponseBody();
			result = new String(responseBody, CharEncoding.UTF_8);
		} catch (HttpException e) {
			throw new PlutusException("HttpClientUtil.doRequestGetJson HttpException", e);
		} catch (IOException e) {
			throw new PlutusException("HttpClientUtil.doRequestGetJson IOException", e);
		} finally {
			if (null != getMethod) {
				getMethod.releaseConnection();
			}
		}
		return result;	
    }

    /**
     * 构造GET请求参数
     * @param urlParamMap
     * @return
     */
    private static List<BasicNameValuePair> generateParamValues(Map<String, Object> urlParamMap) {

        List<BasicNameValuePair> basicNameValuePairs = new LinkedList<BasicNameValuePair>();
        for (Map.Entry<String, Object> entry: urlParamMap.entrySet()) {
            basicNameValuePairs.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
        }
        return basicNameValuePairs;
    }

    /**
     * PUT发送JSON
     * @param uri
     * @param content
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public static String doRequestPutJson(String uri, String content) {
		String result = null;
		PutMethod putMethod  = null;
		try {
			putMethod = new PutMethod(uri);
			RequestEntity entity = new StringRequestEntity(content, ContentType.APPLICATION_JSON.toString(), null);
			putMethod.addRequestHeader("Accept", ContentType.APPLICATION_JSON.toString());
			putMethod.setRequestEntity(entity);
			int statusCode = getHttpClient().executeMethod(putMethod);
			if (statusCode != HttpStatus.SC_OK) {
                logger.error("doRequestPutJson server return no 200, status = {}", statusCode);
                throw new PlutusException("HttpClientUtil.doRequestPutJson call server return no 200, status：" + statusCode);
			}			
            byte[] responseBody = putMethod.getResponseBody();
			result = new String(responseBody, CharEncoding.UTF_8);
		} catch (HttpException e) {
			throw new PlutusException("HttpClientUtil.doRequestPutJson HttpException", e);
		} catch (IOException e) {
			throw new PlutusException("HttpClientUtil.doRequestPutJson IOException", e);
		} finally {
			if (null != putMethod) {
				putMethod.releaseConnection();
			}
		}
		return result;	
    }

    public static String doRequestPutJson(String uri, Map<String, Object> dataMap) {

        HttpPut httpPut = new HttpPut(uri);
        // 设置请求的header
        httpPut.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        String responseEntity = null;

        List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
        formparams.add(new BasicNameValuePair("version", dataMap.get("version").toString()));
        formparams.add(new BasicNameValuePair("request_id", dataMap.get("request_id").toString()));
        formparams.add(new BasicNameValuePair("app_id", dataMap.get("app_id").toString()));
        formparams.add(new BasicNameValuePair("method", dataMap.get("method").toString()));
        formparams.add(new BasicNameValuePair("timestamp", dataMap.get("timestamp").toString()));
        formparams.add(new BasicNameValuePair("format", dataMap.get("format").toString()));
        formparams.add(new BasicNameValuePair("sign_method", dataMap.get("sign_method").toString()));
        formparams.add(new BasicNameValuePair("sign", dataMap.get("sign").toString()));
        formparams.add(new BasicNameValuePair("data", dataMap.get("data").toString()));;
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPut.setEntity(uefEntity);
        } catch (UnsupportedEncodingException e1) {
            logger.error("", e1);
        }
        // 执行请求
        HttpResponse httpResponse;
        CloseableHttpClient closeableHttpClient=null;
        try {
        	closeableHttpClient=httpClientBuilder.build();
            httpResponse = closeableHttpClient.execute(httpPut);
            // 获取响应消息实体
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                try {
                    responseEntity = EntityUtils.toString(entity, "utf-8");
                } catch (ParseException e) {
                    logger.error("", e);
                } catch (IOException e) {
                    logger.error("", e);
                }
//                System.out.println(responseEntity);
            } else {
//                System.out.println("服务器返回值为空");
            }
        } catch (ClientProtocolException e1) {
            logger.error("", e1);
        } catch (IOException e1) {
            logger.error("", e1);
        }finally{
            if(closeableHttpClient!=null){
            	try {
					closeableHttpClient.close();
				} catch (IOException e) {
                    logger.error("', e");
				}
            }
        }
        return responseEntity;
    }
    
    /**
     * DELETE发送JSON
     * @param uri
     * @param content
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public static String doRequestDeleteJson(String uri, String content) throws HttpException, IOException {
		String result = null;
		DeleteMethod deleteMethod = null;
		try {
			deleteMethod = new DeleteMethod(uri);
			deleteMethod.addRequestHeader("Accept", ContentType.APPLICATION_JSON.toString());
			int statusCode = getHttpClient().executeMethod(deleteMethod);
			if (statusCode != HttpStatus.SC_OK) {
                logger.error("doRequestDeleteJson server return no 200, status = {}", statusCode);
                throw new PlutusException("HttpClientUtil.doRequestDeleteJson call server return no 200, status：" + statusCode);
			}	
            byte[] responseBody = deleteMethod.getResponseBody();
			result = new String(responseBody, CharEncoding.UTF_8);
		} catch (HttpException e) {
			throw new HttpException("HttpClientUtil.doRequestDeleteJson HttpException", e);
		} catch (IOException e) {
			throw new IOException("HttpClientUtil.doRequestDeleteJson IOException", e);
		} finally {
			if (null != deleteMethod) {
				deleteMethod.releaseConnection();
			}
		}
		return result;	
    }     
    
    /**
     * 用法： HttpRequestProxy hrp = new HttpRequestProxy();
     * hrp.doRequest("http://www.163.com",null,null,"utf-8");
     * 
     * @param url 请求的资源ＵＲＬ
     * @param postData POST请求时form表单封装的数据 没有时传null
     * @param header request请求时附带的头信息(header) 没有时传null
     * @param encoding response返回的信息编码格式 没有时传null
     * @return response返回的文本数据
     * @throws Exception
     */
    public static String doRequest(String url, Map postData, Map header, String encoding) throws Exception {
        String responseString = null;
        //头部请求信息  
        Header[] headers = initHeader(header);
        if (postData != null) {
            //post方式请求
            logger.info("url:{}, post params:{}", url, postData);
            responseString = executePost(url, postData, encoding, headers);
        } else {
            //get方式 请求
            responseString = executeGet(url, encoding, headers);
        }

        return responseString;
    }

    public static String doRequest(String url) throws Exception {
        String responseString = null;
        //get方式 请求
        responseString = executeGet(url);

        return responseString;
    }

    //get方式 请求
    private static String executeGet(String url) throws Exception {
        String responseString = "";
        GetMethod getRequest = new GetMethod(url.trim());

        try {
            logger.debug("BexecuteGet=" + url);
            responseString = executeMethod(getRequest);
            logger.debug("BexecuteGet=" + url);
        } catch (Exception e) {
            logger.error("HttpClientUtil.executeGet() error", e);
            throw e;
        } finally {
            getRequest.releaseConnection();
        }
        return responseString;
    }

    //get方式 请求
    private static String executeGet(String url, String encoding, Header[] headers) throws Exception {
        String responseString = "";
        GetMethod getRequest = new GetMethod(url.trim());
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                getRequest.setRequestHeader(headers[i]);
            }
        }
        try {
            logger.debug("BexecuteGet=" + url + ", object headers:"
                    + ToStringBuilder.reflectionToString(headers, ToStringStyle.SHORT_PREFIX_STYLE).toString());
            responseString = executeMethod(getRequest, encoding);
            logger.debug("BexecuteGet=" + url + ", object headers:"
                    + ToStringBuilder.reflectionToString(headers, ToStringStyle.SHORT_PREFIX_STYLE).toString());
        } catch (Exception e) {
            logger.error("HttpClientUtil.executeGet() error", e);
            throw e;
        } finally {
            getRequest.releaseConnection();
        }
        return responseString;
    }

    //post方式请求
    private static String executePost(String url, Map postData, String encoding, Header[] headers) throws IOException {
        String responseString = "";
        PostMethod postRequest = new PostMethod(url.trim());
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                postRequest.setRequestHeader(headers[i]);
            }
        }
        Set entrySet = postData.entrySet();
        int dataLength = entrySet.size();
        NameValuePair[] params = new NameValuePair[dataLength];
        int i = 0;
        for (Iterator itor = entrySet.iterator(); itor.hasNext();) {
            Map.Entry entry = (Map.Entry) itor.next();
            params[i++] = new NameValuePair(entry.getKey().toString(), entry.getValue().toString());
        }
        postRequest.setRequestBody(params);
        try {
            logger.debug("BexecutePost=" + url + ", object postData:"
                         + ToStringBuilder.reflectionToString(params, ToStringStyle.SHORT_PREFIX_STYLE).toString());
            responseString = executeMethod(postRequest, encoding);
            logger.debug("EexecutePost=" + url + ", object postData:"
                         + ToStringBuilder.reflectionToString(params, ToStringStyle.SHORT_PREFIX_STYLE).toString());
        } catch (IOException e) {
            throw new IOException("HttpClientUtil.executePost IOException", e);
        } finally {
            postRequest.releaseConnection();
        }
        return responseString;
    }

    //请求头部信息
    private static Header[] initHeader(Map header) {
        Header[] headers = null;
        if (header != null) {
            Set entrySet = header.entrySet();
            int dataLength = entrySet.size();
            headers = new Header[dataLength];
            int i = 0;
            for (Iterator itor = entrySet.iterator(); itor.hasNext();) {
                Map.Entry entry = (Map.Entry) itor.next();
                headers[i++] = new Header(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return headers;
    }

    //调用并获取返回
    private static String executeMethod(HttpMethod request, String encoding) throws IOException {
        String responseContent = null;
        InputStream responseStream = null;
        BufferedReader rd = null;
        try {
            Long start = System.currentTimeMillis();
            logger.debug("B. request param:"
                    + ToStringBuilder.reflectionToString(request, ToStringStyle.SHORT_PREFIX_STYLE).toString());
            int status = getHttpClient().executeMethod(request);
            if(status != HttpStatus.SC_OK){
                logger.error("http call returns not ok, status = {}", status);
                throw new PlutusException("服务调用失败，返回码：" + status);
            }

            logger.debug("E. cost " + (System.currentTimeMillis() - start) + "msec, request param:"
                    + ToStringBuilder.reflectionToString(request, ToStringStyle.SHORT_PREFIX_STYLE).toString());
            if (encoding != null) {
                responseStream = request.getResponseBodyAsStream();
                rd = new BufferedReader(new InputStreamReader(responseStream, encoding));
                String tempLine = rd.readLine();
                StringBuffer tempStr = new StringBuffer();
                String crlf = System.getProperty("line.separator");
                while (tempLine != null) {
                    tempStr.append(tempLine);
                    tempStr.append(crlf);
                    tempLine = rd.readLine();
                }
                responseContent = tempStr.toString();
            } else {
                responseContent = request.getResponseBodyAsString();
            }
            Header locationHeader = request.getResponseHeader("location");
            //返回代码为302,301时，表示页面己经重定向，则重新请求location的url，这在  
            //一些登录授权取cookie时很重要
            //TODO 如果需要处理重定向请求，请在下面代码中改造
            if (locationHeader != null) {
                //String redirectUrl = locationHeader.getValue();
                //doRequest(redirectUrl, null, null, null);
            }
        } catch (HttpException e) {
            throw new HttpException("HttpClientUtil.executeMethod HttpException", e);
        } catch (IOException e) {
            throw new IOException("HttpClientUtil.executeMethod IOException", e);
        } finally {
            if (rd != null)
                try {
                    rd.close();
                } catch (IOException e) {
                    throw new IOException("HttpClientUtil.executeMethod close IOException", e);
                }
            if (responseStream != null)
                try {
                    responseStream.close();
                } catch (IOException e) {
                    throw new IOException("HttpClientUtil.executeMethod close IOException", e);
                }
        }
        return responseContent;
    }

    //调用并获取返回
    @SuppressWarnings("unused")
    private static String executeMethod(HttpMethod request) throws IOException {
        String responseContent = null;
        InputStream responseStream = null;
        ByteArrayOutputStream out = null;
        BASE64Encoder base64 = new BASE64Encoder();
        try {
            Long start = System.currentTimeMillis();
            logger.debug("B. request param:"
                    + ToStringBuilder.reflectionToString(request, ToStringStyle.SHORT_PREFIX_STYLE));
            int status = getHttpClient().executeMethod(request);
            if(status != HttpStatus.SC_OK){
                logger.error("http call returns not ok, status = {}", status);
                throw new PlutusException("服务调用失败，返回码：" + status);
            }
            logger.debug("E. cost " + (System.currentTimeMillis() - start) + "msec, request param:"
                    + ToStringBuilder.reflectionToString(request, ToStringStyle.SHORT_PREFIX_STYLE));
            responseStream = request.getResponseBodyAsStream();
            out = new ByteArrayOutputStream(1024);

            byte[] tempbytes = new byte[1024];
            int byteread = 0;
            while ((byteread = responseStream.read(tempbytes)) != -1) {
                out.write(tempbytes, 0, byteread);
            }
            byte[] bytes = out.toByteArray();
            responseContent = base64.encode(bytes);
            //            responseContent = Base64.encodeBase64String(bytes);//base64加密
            Header locationHeader = request.getResponseHeader("location");
            //返回代码为302,301时，表示页面己经重定向，则重新请求location的url，这在  
            //一些登录授权取cookie时很重要
            //TODO 如果需要处理重定向请求，请在下面代码中改造
            if (locationHeader != null) {
                //String redirectUrl = locationHeader.getValue();
                //doRequest(redirectUrl, null, null, null);
            }
        } catch (HttpException e) {
            throw new HttpException("HttpClientUtil.executeMethod HttpException", e);
        } catch (IOException e) {
            throw new IOException("HttpClientUtil.executeMethod IOException", e);
        } finally {
            if (responseStream != null) {
                responseStream.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return responseContent;
    }

    /**
     * 特殊请求数据,这样的请求往往会出现redirect本身而出现递归死循环重定向 所以单独写成一个请求方法
     * 比如现在请求的url为：http://localhost:8080/demo/index.jsp 返回代码为302
     * 头部信息中location值为:http://localhost:8083/demo/index.jsp
     * 这时httpclient认为进入递归死循环重定向，抛出CircularRedirectException异常
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public String doSpecialRequest(String url, int count, String encoding) throws Exception {
        String str = null;
        InputStream responseStream = null;
        BufferedReader rd = null;
        GetMethod getRequest = new GetMethod(url);
        //关闭httpclient自动重定向动能  
        getRequest.setFollowRedirects(false);
        try {

            client.executeMethod(getRequest);
            Header header = getRequest.getResponseHeader("location");
            if (header != null) {
                //请求重定向后的ＵＲＬ，count同时加1  
                this.doSpecialRequest(header.getValue(), count + 1, encoding);
            }
            //这里用count作为标志位，当count为0时才返回请求的ＵＲＬ文本,  
            //这样就可以忽略所有的递归重定向时返回文本流操作，提高性能  
            if (count == 0) {
                getRequest = new GetMethod(url);
                getRequest.setFollowRedirects(false);
                client.executeMethod(getRequest);
                responseStream = getRequest.getResponseBodyAsStream();
                rd = new BufferedReader(new InputStreamReader(responseStream, encoding));
                String tempLine = rd.readLine();
                StringBuilder tempStr = new StringBuilder();
                String crlf = System.getProperty("line.separator");
                while (tempLine != null) {
                    tempStr.append(tempLine);
                    tempStr.append(crlf);
                    tempLine = rd.readLine();
                }
                str = tempStr.toString();
            }

        } catch (HttpException e) {
            throw new HttpException("HttpClientUtil.doSpecialRequest HttpException", e);
        } catch (IOException e) {
            throw new IOException("HttpClientUtil.doSpecialRequest IOException", e);
        } finally {
            getRequest.releaseConnection();
            if (rd != null)
                try {
                    rd.close();
                } catch (IOException e) {
                    throw new IOException("HttpClientUtil.doSpecialRequest close IOException", e);
                }
            if (responseStream != null)
                try {
                    responseStream.close();
                } catch (IOException e) {
                    throw new IOException("HttpClientUtil.doSpecialRequest close IOException", e);
                }
        }
        return str;
    }

    /**
     * 直接调用HttpGet请求，不进行加解密处理。
     * @param url 请求的url地址
     * @return
     */
    public static String directHttpGet(String url) throws IOException, URISyntaxException {
        BufferedReader in = null;

        String content = null;
        try {

            // 定义HttpClient
            DefaultHttpClient client = new DefaultHttpClient();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line;
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            content = sb.toString();
            return content;
        } catch (Exception e) {
            logger.error("http direct call exception occurs:", e);
            throw new PlutusException("", e);
        } finally {
            if (in != null) {
                try {
                    in.close();// 最后要关闭BufferedReader
                } catch (Exception e) {
                    logger.error("get province info exception occurs.", e);
                }
            }
        }
    }

    public static String retrieveResponseFromServer(String url) {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        try {
            trustAllHttpsCertificates();
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
//                    System.out.println("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            URL validationUrl = new URL(url);
            connection = (HttpURLConnection) validationUrl.openConnection();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            final StringBuffer stringBuffer = new StringBuffer(255);
            synchronized (stringBuffer) {
                while ((line = in.readLine()) != null) {
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                }
                return stringBuffer.toString();
            }

        } catch (Exception e){
            logger.error("", e);
            return null;
        } finally {
        	IOUtils.closeQuietly(in);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {
            return;
        }
    }

    /**
     * 直接发送HttpPost请求
     * @param url
     * @param paramMap
     * @return
     */
    public static String directPost(String url, Map<String, Object> paramMap){

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost method = new HttpPost(url);
        try {
            //解决中文乱码问题
            StringEntity entity = new StringEntity(JSONObject.toJSONString(paramMap), "UTF-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            method.setEntity(entity);
            HttpResponse result = httpClient.execute(method);
            String response = EntityUtils.toString(result.getEntity());
            logger.info("post return:{}", response);
            int statusCode = result.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK){
                logger.warn("post request return, httpStatus:{}", statusCode);
                throw new PlutusException("post请求失败，状态码：" + statusCode);
            }
            /**请求发送成功，并得到响应**/
            return response;
        } catch (IOException e) {
            logger.error("post请求提交失败", e);
            throw new PlutusException(e);
        } finally {
            method.releaseConnection();
            httpClient.close();
        }
    }
    
    public static byte[] doRequestGetForFile(String uri, Map<String, Object> urlParamMap) {

        List<BasicNameValuePair> basicNameValuePairs = generateParamValues(urlParamMap);
        byte[] result = null;
        GetMethod getMethod  = null;
        try {
            getMethod = new GetMethod(uri + "?" + URLEncodedUtils.format(basicNameValuePairs, "UTF-8"));
            getMethod.addRequestHeader("Accept", ContentType.APPLICATION_JSON.toString());
            
            int statusCode = getHttpClient().executeMethod(getMethod);
            if(statusCode != HttpStatus.SC_OK){
                logger.error("doRequestGetJson server return no 200, status = {}", statusCode);
                throw new PlutusException("HttpClientUtil.doRequestGetJson call server return no 200, status：" + statusCode);
            }
            
            result = getMethod.getResponseBody();
            
        } catch (HttpException e) {
            throw new PlutusException("HttpClientUtil.doRequestGetJson HttpException", e);
        } catch (IOException e) {
            throw new PlutusException("HttpClientUtil.doRequestGetJson IOException", e);
        } finally {
            if (null != getMethod) {
                getMethod.releaseConnection();
            }
        }
        return result;  
    }
    
    /**
     * transformMapToUrlParams
     * @param maps
     * @return
     */
	public static String transformMapToUrlParams(Map<String, Object> maps){
		try {
			StringBuffer sb = new StringBuffer();
			for (Map.Entry<String, Object> entry : maps.entrySet()) {
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(URLEncoder.encode(entry.getValue().toString(),"UTF-8"));
				sb.append("&");
			}
			if (sb.length() > 0) {
				sb = sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			logger.warn("transformMapToUrlParams", e);
			return "";
		}
	}
    
    /**
     * 忽略HTTPS请求的SSL证书，必须在openConnection之前调用
     * @throws Exception
     */
    public static void ignoreSsl() throws Exception{  
        HostnameVerifier hv = new HostnameVerifier() {  
            public boolean verify(String urlHostName, SSLSession session) {  
            	logger.warn("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());  
                return true;  
            }  
        };  
        trustAllHttpsCertificates();  
        HttpsURLConnection.setDefaultHostnameVerifier(hv);  
    } 
    
    /**
     * HttpURLConnection POST请求
     * @param urlAddress
     * @param paramMap
     * @return
     * @throws Exception
     */
    public static String sendURLConnectionPost(String urlAddress, Map<String, Object> paramMap) throws Exception{  
    	HttpURLConnection connection = null;
    	OutputStreamWriter osw = null;
        try {
			URL url = new URL(urlAddress);  
			if("https".equalsIgnoreCase(url.getProtocol())){  
			    ignoreSsl();  
			}  
			connection = (HttpURLConnection)url.openConnection();  
			connection.setDoInput(true);  
			connection.setDoOutput(true);  
			connection.setRequestMethod("POST"); 
			connection.setConnectTimeout(6000);  
			connection.setReadTimeout(6000);  
			osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");  
			osw.write(transformMapToUrlParams(paramMap));  
			osw.flush();  
			osw.close();  
			connection.getOutputStream();  
			return IOUtils.toString(connection.getInputStream());
		} catch (Exception e) {
           logger.error("sendURLConnectionPost", e);
           return null;
        } finally {
        	IOUtils.closeQuietly(osw);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }      
    
}
