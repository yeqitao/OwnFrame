package com.tao.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.Part;

public class TAOMultipartFile implements Part{
	private Part part;

	public TAOMultipartFile(Part part) {
		super();
		this.part = part;
	}

	public Part getPart() {
		return part;
	}

	public void setPart(Part part) {
		this.part = part;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String getSubmittedFileName() {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public long getSize() {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public void write(String fileName) throws IOException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void delete() throws IOException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
