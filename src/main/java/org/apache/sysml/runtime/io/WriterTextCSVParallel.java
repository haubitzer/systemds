/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.runtime.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.sysml.conf.DMLConfig;
import org.apache.sysml.hops.OptimizerUtils;
import org.apache.sysml.runtime.controlprogram.parfor.stat.InfrastructureAnalyzer;
import org.apache.sysml.runtime.matrix.data.CSVFileFormatProperties;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;
import org.apache.sysml.runtime.matrix.data.OutputInfo;
import org.apache.sysml.runtime.util.MapReduceTool;

/**
 * 
 */
public class WriterTextCSVParallel extends WriterTextCSV
{
	public WriterTextCSVParallel( CSVFileFormatProperties props ) {
		super( props );
	}

	protected void writeCSVMatrixToHDFS(Path path, JobConf job, FileSystem fs, MatrixBlock src, CSVFileFormatProperties csvprops) 
		throws IOException 
	{
		//estimate output size and number of output blocks (min 1)
		int numPartFiles = (int)(OptimizerUtils.estimateSizeTextOutput(src.getNumRows(), src.getNumColumns(), 
				src.getNonZeros(), OutputInfo.CSVOutputInfo)  / InfrastructureAnalyzer.getHDFSBlockSize());
		numPartFiles = Math.max(numPartFiles, 1);
		
		//determine degree of parallelism
		int numThreads = OptimizerUtils.getParallelTextWriteParallelism();
		numThreads = Math.min(numThreads, numPartFiles);
	
		//fall back to sequential write if dop is 1 (e.g., <128MB) in order to create single file
		if( numThreads <= 1 ) {
			super.writeCSVMatrixToHDFS(path, job, fs, src, csvprops);
			return;
		}
		
		//create directory for concurrent tasks
		MapReduceTool.createDirIfNotExistOnHDFS(path.toString(), DMLConfig.DEFAULT_SHARED_DIR_PERMISSION);
		
		//create and execute tasks
		try 
		{
			ExecutorService pool = Executors.newFixedThreadPool(numThreads);
			ArrayList<WriteCSVTask> tasks = new ArrayList<WriteCSVTask>();
			int rlen = src.getNumRows();
			int blklen = (int)Math.ceil((double)rlen / numThreads);
			for(int i=0; i<numThreads & i*blklen<rlen; i++) {
				Path newPath = new Path(path, String.format("0-m-%05d",i));
				tasks.add(new WriteCSVTask(newPath, job, fs, src, i*blklen, (int)Math.min((i+1)*blklen, rlen), csvprops));
			}

			//wait until all tasks have been executed
			List<Future<Object>> rt = pool.invokeAll(tasks);	
			pool.shutdown();
			
			//check for exceptions 
			for( Future<Object> task : rt )
				task.get();
		} 
		catch (Exception e) {
			throw new IOException("Failed parallel write of csv output.", e);
		}		
	}	

	
	/**
	 * 
	 * 
	 */
	private class WriteCSVTask implements Callable<Object> 
	{
		private JobConf _job = null;
		private FileSystem _fs = null;
		private MatrixBlock _src = null;
		private Path _path =null;
		private int _rl = -1;
		private int _ru = -1;
		private CSVFileFormatProperties _props = null;
		
		public WriteCSVTask(Path path, JobConf job, FileSystem fs, MatrixBlock src, int rl, int ru, CSVFileFormatProperties props) {
			_path = path;
			_job = job;
			_fs = fs;
			_src = src;
			_rl = rl;
			_ru = ru;
			_props = props;
		}

		@Override
		public Object call() throws Exception 
		{
			writeCSVMatrixToFile(_path, _job, _fs, _src, _rl, _ru, _props);
			return null;
		}
	}
}
