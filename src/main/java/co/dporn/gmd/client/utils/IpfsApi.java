package co.dporn.gmd.client.utils;

import java.util.concurrent.CompletableFuture;

import elemental2.dom.Blob;
import elemental2.dom.XMLHttpRequestUpload.OnprogressFn;

public interface IpfsApi {
	CompletableFuture<String> postBlobToIpfs(String ipfsFilename, Blob blob);
	CompletableFuture<String> postBlobToIpfsFile(String filename, Blob blob, OnprogressFn onprogressFn);
	CompletableFuture<String> postBlobToIpfsHlsVideo(String filename, Blob blob, OnprogressFn onprogress);
}