/**
 * Reports a client-side error or crash to our server-side log-crash API endpoint.
 * It formats the error and details exactly as required, then sends to the server setup.
 */
export async function logCrashToApi(error: Error | string | any, codeSourceInfo?: string) {
  try {
    const errorMessage = error instanceof Error ? error.message : (error?.message || String(error));
    
    // Extract stack lines or contextual information to show where the error originated
    let codeSource = codeSourceInfo || '';
    if (!codeSource && error instanceof Error && error.stack) {
      const stackLines = error.stack.split('\n');
      // Take the top 3-4 lines of stack trace which contain the exact file and line numbers
      codeSource = stackLines.map(line => line.trim()).slice(0, 4).join(' \n');
    }
    
    if (!codeSource) {
      codeSource = 'Client-side runtime logic / Browser event handler';
    }

    const payload = {
      errorMessage,
      codeSource
    };

    const response = await fetch('/api/log-crash', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      console.error('Server crash log submission responded with error status:', response.status);
    } else {
      const result = await response.json();
      console.log('Crash log successfully created on server:', result.file);
    }
  } catch (err) {
    console.error('Failed to submit client-side crash log to the server API:', err);
  }
}
